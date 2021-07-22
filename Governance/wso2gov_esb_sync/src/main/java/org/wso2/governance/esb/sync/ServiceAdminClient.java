package org.wso2.governance.esb.sync;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.service.mgt.stub.ServiceAdminException;
import org.wso2.carbon.service.mgt.stub.ServiceAdminStub;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaData;
import org.wso2.carbon.service.mgt.stub.types.carbon.ServiceMetaDataWrapper;

import java.rmi.RemoteException;

public class ServiceAdminClient {

    private ServiceAdminStub stub;

    public ServiceAdminClient(String cookie, String backendServerURL) throws AxisFault {

        String serviceURL = backendServerURL + "ServiceAdmin";
        stub = new ServiceAdminStub(serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setTimeOutInMilliSeconds(15 * 60 * 1000);
        option.setProperty(HTTPConstants.SO_TIMEOUT, 15 * 60 * 1000);
        option.setProperty(HTTPConstants.CONNECTION_TIMEOUT, 15 * 60 * 1000);
        option.setManageSession(true);
        option.setProperty(HTTPConstants.COOKIE_STRING, cookie);

    }

    public ServiceMetaDataWrapper getAllServices(String serviceTypeFilter,
                                                 String serviceSearchString,
                                                 int pageNumber) throws RemoteException {
        return stub.listServices(serviceTypeFilter, serviceSearchString, pageNumber);
    }

    public ServiceMetaData getServiceMetaDta(String serviceName) throws ServiceAdminException, RemoteException {
        return stub.getServiceData(serviceName);
    }

    public Object getWsdl(String serviceName) throws RemoteException {
        return stub.getWSDL(serviceName);
    }
}
