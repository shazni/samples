<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml" indent="yes"/>
<xsl:template match="/">
    <html>
        <head>
            <title>
                <xsl:value-of select="//*[local-name() = 'first_name']"/> <xsl:value-of select="//*[local-name() = 'last_name']"/> - Result Sheet for Semester <xsl:value-of select="(//*[local-name() = 'semester'])[position()=1]/text()"/></title>
        </head>
        <body bgcolor="#ffffff">
            <p>Hi <xsl:value-of select="//*[local-name() = 'first_name']"/>,</p>
            <p>Please find below the result</p>
            <table border = "1">
                <tr bgcolor = "#9acd32">
                    <th>Subject Name</th>
                    <th>Result</th>
                </tr>
                <xsl:for-each select="//*[local-name() = 'subject_result']">
                    <tr>
                    <td><xsl:value-of select = "current()//*[local-name() = 'subject']"/></td>
                    <td><xsl:value-of select = "current()//*[local-name() = 'grade']"/></td>
                    </tr>
                </xsl:for-each>
            </table>
            <p>Congratulation on getting through the exam. Keep up the good work</p>
            <p>Thank you</p>
        </body>
    </html>
</xsl:template>
</xsl:stylesheet>