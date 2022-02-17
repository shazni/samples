<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0" exclude-result-prefixes="">
    <xsl:variable name="nationalities">
        <xsl:for-each select="//jsonObject/capabilities/acceptedNationalities">
            <acceptedNationalities><xsl:value-of select="."/></acceptedNationalities>
        </xsl:for-each>
    </xsl:variable>

    <xsl:template match="//jsonObject"/>

    <xsl:template match="//jsonArray">
        <jsonArray xmlns="http://ws.apache.org/ns/synapse">
            <xsl:for-each select="jsonElement">
                <jsonElement>
                    <stayId>
                        <xsl:value-of select="stayId"/>
                    </stayId>
                    <hhonorsNumber>
                        <xsl:value-of select="guest/hhonorsNumber"/>
                    </hhonorsNumber>
                    <propCd>
                        <xsl:value-of select="propCode"/>
                    </propCd>
                    <xsl:copy-of select="$nationalities"/>
                </jsonElement>
            </xsl:for-each>
        </jsonArray>
    </xsl:template>
</xsl:stylesheet>