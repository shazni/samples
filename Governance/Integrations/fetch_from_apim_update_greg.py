import requests
import logging
import json
import re
from base64 import b64encode

logFile = 'sync_api.log'
logging.basicConfig(filename=logFile, level=logging.DEBUG,
        format='%(asctime)s - %(name)-18s - line %(lineno)-5d - %(message)s')
logger = logging.getLogger("syc_accounts")
logger.setLevel(logging.DEBUG)

token = '681a468c-eed1-3883-b88f-b70af6a60a09'
apim_api_base_url = 'https://localhost:9443'
# governance_swagger_base_url = 'https://nginx.wso2.governance.com//governance/swaggers'
governance_swagger_base_url = 'https://wso2.governance.com/governance/swaggers'
# governance_swagger_base_url = 'https://internal.sandbox-governance.wso2.com//governance/swaggers'
# apim_api_context = '/api/am/publisher/v1.0/apis?limit=25&offset=0'
# apim_api_context = '/api/am/publisher/v1/apis?limit=25&offset=0'
apim_api_context = '/api/am/publisher/v2/apis'    # Use this fot API-M 4.0.0

ADMIN_USER_NAME = 'admin'
ADMIN_PWD = 'admin'
#ADMIN_PWD = 'ws02sa123'

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

def get_all_apis():
    # Note we are only fetching 25 here
    all_apis = requests.get(url = apim_api_base_url + apim_api_context + '?limit=25&offset=0', 
        headers=get_authentication_headers('admin', 'admin'), verify=False)
    return all_apis

def get_api_detail(id):
    #detail_api = requests.get(url = apim_api_base_url + '/api/am/publisher/v1.0/apis/' + id, 
    detail_api = requests.get(url = apim_api_base_url + apim_api_context + '/' + id, 
        headers=get_authentication_headers('admin', 'admin'), verify=False)
    
    return detail_api

def get_swagger(id):
    #api_swagger = requests.get(url = apim_api_base_url + '/api/am/publisher/v1.0/apis/' + id + '/swagger', 
    api_swagger = requests.get(url = apim_api_base_url + apim_api_context + '/' + id + '/swagger', 
        headers=get_authentication_headers('admin', 'admin'), verify=False)
    return api_swagger

def create_new_swagger_in_greg(swagger_payload):
    create_swagger_response = requests.post(url = governance_swagger_base_url, 
        headers=get_authentication_headers('admin', ADMIN_PWD), 
        data=json.dumps(swagger_payload), verify=False)

    print("Create SWAGGER RESPONSE = " + str(create_swagger_response.status_code))

def sync_apis():
    all_api = get_all_apis()
    print(all_api)
    all_api_json = all_api.json()
    print(all_api_json)

    if len(all_api_json['list']) > 0:
        for api_obj in all_api_json['list']:
            api_id = api_obj['id']
            api_name = api_obj['name']
            api_context = api_obj['context']
            api_version = api_obj['version']

            print("Fetching API with id = " + api_id)
            print("API name = " + api_name + " context = " + api_context + " version = " + api_version)
            api_detail = get_api_detail(api_id)
            api_detail_json_obj = api_detail.json()
            print(api_detail_json_obj)

            if api_detail_json_obj['type'] == "HTTP":
                print("API is of HTTP type, fetching the Swagger")

                api_swagger = get_swagger(api_id).json()

                print("######## SWAGGER ##########")
                print(api_swagger)

                if api_swagger.get('openapi') != None:
                    api_swagger.pop('openapi')
                    if api_swagger.get('swagger') == None:
                        api_swagger['swagger'] = '2.0'

                print("Let's create swagger" + api_name + ".json in G-Reg")

                greg_swagger_payload = {}
                greg_swagger_payload['name'] = api_name + '.json'
                greg_swagger_payload['type'] = 'swagger'
                greg_swagger_payload['overview_version'] = api_version
                greg_swagger_payload['asset_content'] = json.dumps(api_swagger)

                create_new_swagger_in_greg(greg_swagger_payload)

if __name__ == '__main__':
    logger.info("Staring sync...")
    sync_apis()
    logger.info("Syncing successfully completed")
