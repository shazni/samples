package org.wso2.governance.esb.sync;

import org.wso2.governance.esb.sync.ServiceAdminClient;

public class DataHolder {
    private static String esbUserName = "admin";
    private static String esbPassword = "admin";
    private static String esbServicesURL = "https://localhost:9443/services/";

    private static String esbTrustoreLocation;
    private static String esbTrustorePassword = "wso2carbon";
    private static String esbTrustoreType = "JKS";

    private static String governanceUserName = "admin";
    private static String governancePassword = "admin";
    private static String governanceWSDLURL = "https://nginx.wso2.governance.com/governance/wsdls";

    private static String govTrustoreLocation;
    private static String govTrustorePassword = "wso2carbon";
    private static String govTrustoreType = "JKS";

    public static String getEsbTrustoreLocation() {
        return esbTrustoreLocation;
    }

    public static void setEsbTrustoreLocation(String esbTrustoreLocation) {
        DataHolder.esbTrustoreLocation = esbTrustoreLocation;
    }

    public static String getEsbTrustorePassword() {
        return esbTrustorePassword;
    }

    public static void setEsbTrustorePassword(String esbTrustorePassword) {
        DataHolder.esbTrustorePassword = esbTrustorePassword;
    }

    public static String getEsbTrustoreType() {
        return esbTrustoreType;
    }

    public static void setEsbTrustoreType(String esbTrustoreType) {
        DataHolder.esbTrustoreType = esbTrustoreType;
    }

    public static String getGovTrustoreLocation() {
        return govTrustoreLocation;
    }

    public static void setGovTrustoreLocation(String govTrustoreLocation) {
        DataHolder.govTrustoreLocation = govTrustoreLocation;
    }

    public static String getGovTrustorePassword() {
        return govTrustorePassword;
    }

    public static void setGovTrustorePassword(String govTrustorePassword) {
        DataHolder.govTrustorePassword = govTrustorePassword;
    }

    public static String getGovTrustoreType() {
        return govTrustoreType;
    }

    public static void setGovTrustoreType(String govTrustoreType) {
        DataHolder.govTrustoreType = govTrustoreType;
    }

    private static String cookie;
    private static ServiceAdminClient serviceAdminClient;

    public static String getEsbUserName() {
        return esbUserName;
    }

    public static void setEsbUserName(String esbUserName) {
        DataHolder.esbUserName = esbUserName;
    }

    public static String getEsbPassword() {
        return esbPassword;
    }

    public static void setEsbPassword(String esbPassword) {
        DataHolder.esbPassword = esbPassword;
    }

    public static String getEsbServicesURL() {
        return esbServicesURL;
    }

    public static void setEsbServicesURL(String esbServicesURL) {
        DataHolder.esbServicesURL = esbServicesURL;
    }

    public static String getGovernanceUserName() {
        return governanceUserName;
    }

    public static void setGovernanceUserName(String governanceUserName) {
        DataHolder.governanceUserName = governanceUserName;
    }

    public static String getGovernancePassword() {
        return governancePassword;
    }

    public static void setGovernancePassword(String governancePassword) {
        DataHolder.governancePassword = governancePassword;
    }

    public static String getGovernanceWSDLURL() {
        return governanceWSDLURL;
    }

    public static void setGovernanceWSDLURL(String governanceWSDLURL) {
        DataHolder.governanceWSDLURL = governanceWSDLURL;
    }

    public static ServiceAdminClient getServiceAdminClient() {
        return serviceAdminClient;
    }

    public static void setServiceAdminClient(ServiceAdminClient _serviceAdminClient) {
        serviceAdminClient = _serviceAdminClient;
    }

    public static void setCookie(String _cookie) {
        cookie = _cookie;
    }

    public static String getCookie() {
        return cookie;
    }
}
