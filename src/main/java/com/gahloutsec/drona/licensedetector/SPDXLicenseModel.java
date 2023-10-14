

package com.gahloutsec.drona.licensedetector;

/**
 *
 * @author Pratham Gahlout
 */
public class SPDXLicenseModel {
    private String reference;
    private Boolean isDeprecatedLicenseId;
    private String detailsUrl;
    private String referenceNumber;
    private String name;
    private String licenseId;
    private Boolean isOsiApproved;

    public SPDXLicenseModel(String reference, Boolean isDeprecatedLicenseId, String detailsUrl, String referenceNumber, String name, String licenseId, Boolean isOsiApproved) {
        this.reference = reference;
        this.isDeprecatedLicenseId = isDeprecatedLicenseId;
        this.detailsUrl = detailsUrl;
        this.referenceNumber = referenceNumber;
        this.name = name;
        this.licenseId = licenseId;
        this.isOsiApproved = isOsiApproved;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Boolean getIsDeprecatedLicenseId() {
        return isDeprecatedLicenseId;
    }

    public void setIsDeprecatedLicenseId(Boolean isDeprecatedLicenseId) {
        this.isDeprecatedLicenseId = isDeprecatedLicenseId;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }

    public Boolean getIsOsiApproved() {
        return isOsiApproved;
    }

    public void setIsOsiApproved(Boolean isOsiApproved) {
        this.isOsiApproved = isOsiApproved;
    }
    
    
}
