from mysql.connector import MySQLConnection, Error
from python_mysql_dbconfig import read_config
from urllib.parse import urlencode
import requests
from base64 import b64encode
import status
import datetime
import logging
import json
import re

logFile = 'sync_account.log'
logging.basicConfig(filename=logFile, level=logging.DEBUG,
        format='%(asctime)s - %(name)-18s - line %(lineno)-5d - %(message)s')
logger = logging.getLogger("syc_accounts")
logger.setLevel(logging.DEBUG)

customer_status_actions = {
    "BECAME_A_CUSTOMER" : "Became a customer",
    "LOST_TO_COMPETITOR" : "Lost to competitor",
    "LOST_PRODUCT_MISMATCH" : "Lost - Product did not match",
    "LOST_PROJECT_CANCELLED" : "Lost - Project cancelled",
    "LOST_USING_OSS" : "Lost - Using opensource",
    "LOST_NO_BUDGET" : "Lost - No budget"
}

customer_statuses = {
    "PROSPECT" : "Prospect",
    "CUSTOMER" : "Customer",
    "LOST" : 'Lost'
}

def get_accept_content_type_headers():
    return {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    }

def get_authentication_headers(username, password):
    authentication_headers = get_accept_content_type_headers()
    authentication_headers['Authorization'] = \
        'Basic ' + b64encode((username + ':' + password).encode('utf-8')).decode('utf-8')
    return authentication_headers
 
def iter_row(cursor, size=10):
    while True:
        rows = cursor.fetchmany(size)
        if not rows:
            break
        for row in rows:
            yield row

def get_arr(cursor, account_id):
    # Below should come from the SF_OPPORTUNITY
    today_date = datetime.datetime.now().strftime('%Y-%m-%d')

    arr_query = "SELECT CurrencyIsoCode, SUM(o.ARR__c) FROM UnifiedDashboards.SF_OPPORTUNITY " + \
        "AS o JOIN UnifiedDashboards.SF_ACCOUNT AS a ON o.AccountId = a.Id AND " + \
        "o.StageName in (\"50 - Closed Won\", \"49 - Closed Won (Partial Contracts)\") AND " + \
        "o.ARR__c > 0 AND " + \
        "o.PS_Support_Account_Start_Date_Roll_Up__c <= DATE(\'" + today_date + "\') AND " + \
        "o.PS_Support_Account_End_Date_Roll_Up__c >= DATE(\'" + today_date + "\') AND " + \
        "o.IsInSF = 1 AND " + \
        "a.Id = '" + account_id + "'"

    logger.info("ARR check query : {}".format(arr_query))
    cursor.execute(arr_query)
    arr_row = cursor.fetchone()

    if arr_row[0] != None:
        logger.debug("Customer id : {}, arr : {}".format(arr_row[0], arr_row[1]))
        return (arr_row[0], arr_row[1])
    else:
        return None

def get_primary_technical_owner(cursor, account_id):
    # Finding the TO from the last opportunity recorded
    # Ideally this should be an account level details which is currently missing.

    latest_opp_query = "select Technical_Owner__c from SF_OPPORTUNITY where AccountId='" + account_id + \
        "' and CloseDate = (select MAX(CloseDate) from SF_OPPORTUNITY where AccountId='" + \
        account_id + "')"
    logger.info("Latest opp query : {}".format(latest_opp_query))
    cursor.execute(latest_opp_query)
    latest_opp_row = cursor.fetchone()

    if latest_opp_row != None:
        logger.debug("Primary technical owner : {}".format(latest_opp_row[0]))
        return latest_opp_row[0]
    else:
        return None

def create_new_customer(g_reg_config, data):
    customer_create_response = requests.post(url = g_reg_config['g_reg_customer_url'], 
        headers=get_authentication_headers(g_reg_config['service_username'], g_reg_config['service_pwd']), 
        data=json.dumps(data), verify=False)

    if customer_create_response.status_code == status.HTTP_200_OK or \
        customer_create_response.status_code == status.HTTP_201_CREATED:
        logger.info("New customer {} created successfully".format(data['name']))
    elif customer_create_response.status_code == status.HTTP_401_UNAUTHORIZED:
        logger.info("Authentication failed while creating the customer".format(data['name']))
    else:
        logger.error("Creating customer {} failed. status {}".format(data['name'], customer_create_response.status_code))

    return customer_create_response

def update_customer(g_reg_config, asset_id, data):
    update_asset_url = g_reg_config['g_reg_customer_url'] + asset_id
    customer_update_response = requests.put(url = update_asset_url, 
        headers=get_authentication_headers(g_reg_config['service_username'], g_reg_config['service_pwd']), 
        data=json.dumps(data), verify=False)

    if customer_update_response.status_code == status.HTTP_200_OK or \
        customer_update_response.status_code == status.HTTP_201_CREATED:
        logger.info("Customer {} updated successfully".format(data['name']))
    elif customer_update_response.status_code == status.HTTP_401_UNAUTHORIZED:
        logger.info("Authentication failed while updating the customer".format(data['name']))
    else:
        logger.error("Customer update {} failed. status : {}".format(data['name'], customer_update_response.status_code))

    return customer_update_response

def change_customer_status(g_reg_config, asset_id, state):
    update_lc_url = g_reg_config['g_reg_customer_url'] + asset_id + "/states"
    data = { "lc":"CustomerLifeCycle", "action": state }

    change_status_response = requests.put(url = update_lc_url, 
        headers=get_authentication_headers(g_reg_config['service_username'], g_reg_config['service_pwd']), 
        data=json.dumps(data), verify=False)

    if change_status_response.status_code == status.HTTP_200_OK:
        logger.info("Customer asset {}'s status updated successfully".format(asset_id))
    elif customer_create_response.status_code == status.HTTP_401_UNAUTHORIZED:
        logger.info("Authentication failed while changing the customer asset {}".format(asset_id))
    else:
        logger.error("Customer asset {}'s status update failed. status : {}".format(asset_id, change_status_response.status_code))

    return change_status_response

def get_customer_status(g_reg_config, asset_id):
    get_lc_url = g_reg_config['g_reg_customer_url'] + asset_id + "/states"
    lc_param = { 'lc' : 'CustomerLifeCycle' }

    customer_status_response = requests.get(url = get_lc_url, params=lc_param,
        headers=get_authentication_headers(g_reg_config['service_username'], g_reg_config['service_pwd']), 
        verify=False)

    if customer_status_response.status_code == status.HTTP_200_OK:
        customer_status = customer_status_response.json()['states'][0]['state']
        logger.debug("Customer asset {}'s status is {}".format(asset_id, customer_status))
        return customer_status

def get_customer_asset(g_reg_config, account_name):
    name_param = { 'name' : account_name }
    name_param = urlencode(name_param)
    account_response = requests.get(url = g_reg_config['g_reg_customer_url'], 
        params = name_param, 
        headers=get_authentication_headers(g_reg_config['service_username'], 
            g_reg_config['service_pwd']), verify=False)

    return account_response

    
def sync_accounts():
    """ Connect to MySQL database and sync account details with G-Reg """
 
    db_config = read_config()
    conn = None
    
    try:
        logger.debug('Connecting to MySQL database...')
        conn = MySQLConnection(**db_config)
 
        if conn.is_connected():
            logger.debug('Connection established.')
            cursor = conn.cursor(buffered=True)

            cursor.execute("select distinct(Sub_Region__c) from SF_ACCOUNT")
            sub_regions = cursor.fetchall()

            logger.debug('Total Distict Sub-regions(s) : {}'.format(cursor.rowcount))
            print(sub_regions)
            for sub_region in sub_regions:
                # For each sub-region we are going to fetch the customers and prospects
                sub_region = sub_region[0]  # Because each row fetched is a tuple
                if sub_region == 'null' or sub_region == 'NULL' or sub_region == None:
                    continue
                print("Sub region - " + sub_region)
                logger.debug("Checking for region" + sub_region)

                account_query = "select Name, Id, Account_Status__c, AnnualRevenue, " + \
                    "Closed_Won_Opportunity_Value_New__c, Industry, Lost_Date__c, " + \
                    "BillingCountry, BillingState, Owner, Verified_Lost_Account__c, " + \
                    "Sales_Regions__c, Sub_Region__c, Region_Bucket__c, Global_POD__c from SF_ACCOUNT " + \
                    "where Sub_Region__c = \"" + sub_region + "\""
                logger.info("Fecthing account of pod : {}, query : {}".format(sub_region, account_query))
                cursor.execute(account_query)

                account_rows = cursor.fetchall()
                logger.info('Total account information found for region {} = {}:'.format(sub_region, cursor.rowcount))

                for row in account_rows:
                    print("Account detail : " + str(row))
                    logger.debug("Account detail : {}".format(str(row)))
                    
                    if len(row) < 15:
                        logger.error("Requested for at least 15 attributes. But got less than that. Thus skipping...")
                        continue

                    account_name = row[0] if row[0] is not None else ''

                    # Checking for any special characters
                    account_name = re.sub('[@_!#$%^&*()<>?/\|}{~:]', '', account_name)

                    account_id = row[1] if row[1] is not None else ''
                    account_status = row[2] if row[2] is not None else ''

                    if account_status == 'Lost Customer' or account_status == 'Lost Prospect':
                        print("Skipping as the customer is already lost")
                        logger.debug("Skipping adding {} as it is an already lost customer/prospect".format(account_name))
                        # For now, skipping the already Lost customers and prospects
                        continue

                    account_industry = row[5] if row[5] is not None else ''
                    account_country = row[7] if row[7] is not None else ''
                    account_am = row[9] if row[9] is not None else ''
                    account_region = row[11] if row[11] is not None else ''
                    account_pod = row[12] if row[12] is not None else ''

                    g_reg_config = read_config('config.ini', 'g-reg')
                    # Check if the account is already available in the G-Reg

                    account_response = get_customer_asset(g_reg_config, account_name)

                    if account_response.status_code == status.HTTP_200_OK:
                        # We are checking for an exact name. So we should only expect 1 value
                        account_response_json = account_response.json()
                        account_json_obj = account_response_json['assets'][0]
                        asset_id = account_json_obj.pop('id')
                        logger.info("Custom {} already exists in G-Reg. Assset ID : {}".format(account_name, asset_id))
                        
                        asset_link = account_json_obj.pop('self-link')
                        asset_content_link = account_json_obj.pop('content-link')

                        account_update_exist = False
                        (account_json_obj['sales_country'], account_update_exist) = \
                            (account_country, True) if account_json_obj['sales_country'] != account_country \
                                else (account_json_obj['sales_country'], account_update_exist)

                        (account_json_obj['sales_region'], account_update_exist) = \
                            (account_region, True) if account_json_obj['sales_region'] != account_region \
                                else (account_json_obj['sales_region'], account_update_exist)

                        (account_json_obj['sales_pod'], account_update_exist) = \
                            (account_pod, True) if account_json_obj['sales_pod'] != account_pod \
                                else (account_json_obj['sales_pod'], account_update_exist)

                        (account_json_obj['sales_am'], account_update_exist) = \
                            (account_am, True) if account_json_obj['sales_am'] != account_am \
                                else (account_json_obj['sales_am'], account_update_exist)

                        (account_json_obj['sales_businessDomain'], account_update_exist) = \
                            (account_industry, True) if account_json_obj['sales_businessDomain'] != account_industry \
                                else (account_json_obj['sales_businessDomain'], account_update_exist)

                        technical_owner_info = get_primary_technical_owner(cursor, account_id)
                        primary_technical_owner = technical_owner_info if technical_owner_info != None else '--- none ---'

                        (account_json_obj['ownership_primaryTechnicalOwner'], account_update_exist) = \
                            (primary_technical_owner, True) if account_json_obj['ownership_primaryTechnicalOwner'] != primary_technical_owner \
                                else (account_json_obj['ownership_primaryTechnicalOwner'], account_update_exist)

                        arr_info = get_arr(cursor, account_id)

                        if arr_info != None and len(arr_info) > 1:
                            if account_json_obj.get('sales_pricingCurrency', None) == None:
                                account_json_obj['sales_pricingCurrency'] = ''
                            (account_json_obj['sales_pricingCurrency'], account_update_exist) = \
                                (arr_info[0], True) if account_json_obj['sales_pricingCurrency'] != arr_info[0] \
                                    else (account_json_obj['sales_pricingCurrency'], account_update_exist)

                            if account_json_obj.get('sales_arr', None) == None:
                                account_json_obj['sales_arr'] = '0.0'
                            (account_json_obj['sales_arr'], account_update_exist) = \
                                (arr_info[1], True) if account_json_obj['sales_arr'] != arr_info[1] \
                                    else (account_json_obj['sales_arr'], account_update_exist)

                        if account_update_exist == True:
                            logger.debug("Updating the customer {} as there are updates".format(account_name))
                            update_customer(g_reg_config, asset_id, account_json_obj)

                        customer_status = get_customer_status(g_reg_config, asset_id)
                        if customer_status != customer_statuses['LOST'] and customer_status != account_status:
                            if customer_status == customer_statuses['PROSPECT']:
                                change_customer_status(g_reg_config, asset_id, 
                                    customer_status_actions['BECAME_A_CUSTOMER'])
                            if customer_status == customer_statuses['CUSTOMER']:
                                change_customer_status(g_reg_config, asset_id, 
                                    customer_status_actions['LOST_TO_COMPETITOR'])

                    elif account_response.status_code == status.HTTP_404_NOT_FOUND:
                        account_data_payload = {}
                        account_data_payload['name'] = account_name
                        account_data_payload['type'] = 'customer'

                        # Strip off last three characters from the Id to create the link ID
                        account_data_payload['sfdcLink'] = g_reg_config['sf_url'] + account_id[:-3]
                        account_data_payload['sales_country'] = account_country
                        account_data_payload['sales_region'] = account_region
                        account_data_payload['sales_pod'] = account_pod
                        account_data_payload['sales_am'] = account_am
                        account_data_payload['sales_businessDomain'] = account_industry.replace(' & ', ' &amp; ')

                        arr_info = get_arr(cursor, account_id)

                        if arr_info != None and len(arr_info) > 1:
                            account_data_payload['sales_pricingCurrency'] = arr_info[0]
                            account_data_payload['sales_arr'] = arr_info[1]
                        else:
                            account_data_payload['sales_pricingCurrency'] = 'USD'
                            account_data_payload['sales_arr'] = '0.0'

                        technical_owner_info = get_primary_technical_owner(cursor, account_id)
                        primary_technical_owner = technical_owner_info if technical_owner_info != None else '--- none ---'

                        account_data_payload['ownership_primaryTechnicalOwner'] = primary_technical_owner

                        new_customer_resposne = create_new_customer(g_reg_config, account_data_payload)

                        # Immediately changing status won't work as G-Reg needs to index the data
                        # So it's kept to be updated in subsequent syncing
                        # if new_customer_resposne.status_code == status.HTTP_200_OK or \
                        #     new_customer_resposne.status_code == status.HTTP_201_CREATED:
                        #     new_account_respose = get_customer_asset(g_reg_config, account_name)
                        #     if account_status == "Lost Customer" or account_status == "Customer":
                        #         change_customer_status(g_reg_config, new_account_respose.json()['assets'][0]['id'], 
                        #             customer_status_actions['BECAME_A_CUSTOMER'])
                        #     if account_status == "Lost Customer" or account_status == "Lost Prospect":
                        #         change_customer_status(g_reg_config, new_account_respose.json()['assets'][0]['id'], 
                        #             customer_status_actions['LOST_TO_COMPETITOR'])

                    elif account_response.status_code == status.HTTP_401_UNAUTHORIZED:
                        logger.warn("unauthorized to check for accounts")
                        
                    else:
                        logger.error("Something is wrong in the request")

        else:
            logger.error('Connection failed.')
 
    except Error as error:
        logger.error(error)
 
    finally:
        if conn is not None and conn.is_connected():
            conn.close()
            logger.debug('Connection closed.')
 
if __name__ == '__main__':
    logger.info("Staring sync...")
    sync_accounts()
    logger.info("Syncing successfully completed")
