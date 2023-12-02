<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
        <xsl:template match="/">
        <result><xsl:text>&#xA;</xsl:text>
                <xsl:for-each select="//*[local-name() = 'subject_result']">
                    <xsl:value-of select = "current()//*[local-name() = 'subject']"/><xsl:text>,</xsl:text><xsl:value-of select = "current()//*[local-name() = 'grade']"/><xsl:text>&#xA;</xsl:text>
                </xsl:for-each>
        </result>
        </xsl:template>
</xsl:stylesheet>