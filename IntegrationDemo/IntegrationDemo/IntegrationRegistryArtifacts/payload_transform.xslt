<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
      <xsl:template match="/">
         <order xmlns="http://ws.apache.org/ns/synapse">
            <xsl:for-each select="//*[local-name() = 'orderDetails']">
               <xsl:variable name="recordType" select="current()//*[local-name() = 'recordType']"/>
<xsl:variable name="recordDetails" select="current()//*[local-name() = 'recordDetails']"/>
               <xsl:if test="$recordType = '0000'">
                  <orderHeader>
                     <recType>0000</recType>
                     <fileId><xsl:value-of select="normalize-space(substring($recordDetails,1,7))" /></fileId>
                  </orderHeader>
               </xsl:if>
               <xsl:if test="$recordType = '0020'">
                  <orderDetails>
                     <recType>0020</recType>
                     <suppGrpCd><xsl:value-of select="normalize-space(substring($recordDetails,1,5))" /></suppGrpCd>
                     <suppGrpLoc><xsl:value-of select="normalize-space(substring($recordDetails,6,2))" /></suppGrpLoc>
                     <dept><xsl:value-of select="normalize-space(substring($recordDetails,8,4))" /></dept>
                     <comm><xsl:value-of select="normalize-space(substring($recordDetails,12,4))" /></comm>
                     <filler><xsl:value-of select="normalize-space(substring($recordDetails,16))" /></filler>
                  </orderDetails>
               </xsl:if>
               <xsl:if test="$recordType = '9999'">
                  <orderTrailer>
                     <recType>9999</recType>
                     <fileId><xsl:value-of select="normalize-space(substring($recordDetails,1,7))" /></fileId>
                  </orderTrailer>
               </xsl:if>
            </xsl:for-each>
         </order>
      </xsl:template>
   </xsl:stylesheet>
                                    
