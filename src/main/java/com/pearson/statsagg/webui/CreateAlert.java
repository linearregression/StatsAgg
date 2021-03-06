package com.pearson.statsagg.webui;

import com.pearson.statsagg.database_objects.DatabaseObjectCommon;
import java.io.PrintWriter;
import java.math.BigDecimal;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.pearson.statsagg.database_objects.alerts.Alert;
import com.pearson.statsagg.database_objects.alerts.AlertsDao;
import com.pearson.statsagg.database_objects.metric_group.MetricGroup;
import com.pearson.statsagg.database_objects.metric_group.MetricGroupsDao;
import com.pearson.statsagg.database_objects.notifications.NotificationGroup;
import com.pearson.statsagg.database_objects.notifications.NotificationGroupsDao;
import com.pearson.statsagg.globals.GlobalVariables;
import com.pearson.statsagg.utilities.StackTrace;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.owasp.encoder.Encode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jeffrey Schmidt
 */
@WebServlet(name = "CreateAlert", urlPatterns = {"/CreateAlert"})
public class CreateAlert extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(CreateAlert.class.getName());
    
    public static final String PAGE_NAME = "Create Alert";
    
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        processGetRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        processPostRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return PAGE_NAME;
    }
    
    protected void processGetRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        response.setContentType("text/html");
        PrintWriter out = null;
    
        try {  
            StringBuilder htmlBuilder = new StringBuilder();

            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            
            Alert alert = null;
            String name = request.getParameter("Name");
            if (name != null) {
                AlertsDao alertsDao = new AlertsDao();
                alert = alertsDao.getAlertByName(name.trim());
            }        

            String htmlBodyContents = buildCreateAlertHtml(alert);
            List<String> additionalJavascript = new ArrayList<>();
            additionalJavascript.add("js/statsagg_create_alert.js");
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContents, additionalJavascript, false);
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        }

    }
    
    protected void processPostRequest(HttpServletRequest request, HttpServletResponse response) {
        
        if ((request == null) || (response == null)) {
            return;
        }
        
        PrintWriter out = null;
        
        try {
            String result = parseAndAlterAlert(request);
            
            response.setContentType("text/html");     
            
            StringBuilder htmlBuilder = new StringBuilder();
            StatsAggHtmlFramework statsAggHtmlFramework = new StatsAggHtmlFramework();
            String htmlHeader = statsAggHtmlFramework.createHtmlHeader("StatsAgg - " + PAGE_NAME, "");
            String htmlBodyContent = statsAggHtmlFramework.buildHtmlBodyForPostResult(PAGE_NAME, StatsAggHtmlFramework.htmlEncode(result), "Alerts", Alerts.PAGE_NAME);
            String htmlBody = statsAggHtmlFramework.createHtmlBody(htmlBodyContent);
            htmlBuilder.append("<!DOCTYPE html>\n<html>\n").append(htmlHeader).append(htmlBody).append("</html>");
            
            Document htmlDocument = Jsoup.parse(htmlBuilder.toString());
            String htmlFormatted  = htmlDocument.toString();
            out = response.getWriter();
            out.println(htmlFormatted);
        }
        catch (Exception e) {
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
        finally {            
            if (out != null) {
                out.close();
            }
        }
    }

    private String buildCreateAlertHtml(Alert alert) {
        
        StringBuilder htmlBody = new StringBuilder();
        
        htmlBody.append(
            "<div id=\"page-content-wrapper\">\n" +
            "  <!-- Keep all page content within the page-content inset div! -->\n" +
            "  <div class=\"page-content inset statsagg_page_content_font\">\n" +
            "  <div class=\"content-header\"> \n" +
            "    <div class=\"pull-left content-header-h2-min-width-statsagg\"> <h2> " + PAGE_NAME + " </h2> </div>\n" +
            "  </div>\n " +
            "  <form action=\"CreateAlert\" method=\"POST\">\n" +
            "    <div class=\"row create-alert-form-row\">\n");

        if ((alert != null) && (alert.getName() != null) && !alert.getName().isEmpty()) {
            htmlBody.append("<input type=\"hidden\" name=\"Old_Name\" value=\"").append(Encode.forHtmlAttribute(alert.getName())).append("\">");
        }
        
        
        // start column 1
        htmlBody.append(
            "<div class=\"col-md-4 statsagg_three_panel_first_panel\">\n" +
            "  <div class=\"panel panel-default\">\n" +
            "    <div class=\"panel-heading\"><b>Core Alert Criteria</b></div>\n" +
            "    <div class=\"panel-body\">");
            
        
        // name
        htmlBody.append(      
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Name</label>\n" +
            "  <button type=\"button\" id=\"Name_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A unique name for this alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"Name\" id=\"Name\" ");
        
        if ((alert != null) && (alert.getName() != null)) {
            htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(alert.getName())).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // description
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Description</label>\n" +
            "  <textarea class=\"form-control-statsagg\" rows=\"3\" name=\"Description\" id=\"Description\">");

        if ((alert != null) && (alert.getDescription() != null)) {
            htmlBody.append(Encode.forHtmlAttribute(alert.getDescription()));
        }

        htmlBody.append("</textarea>\n");
        htmlBody.append("</div>\n");

        
        // metric group name
        htmlBody.append(
            "<div class=\"form-group\" id=\"MetricGroupName_Lookup\">\n" +
            "  <label class=\"label_small_margin\">Metric group name</label>\n" +
            "  <button type=\"button\" id=\"MetricGroupName_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the metric group to associate with this alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"MetricGroupName\" id=\"MetricGroupName\" ");

        if ((alert != null) && (alert.getMetricGroupId() != null)) {
            MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
            MetricGroup metricGroup = metricGroupsDao.getMetricGroup(alert.getMetricGroupId());

            if ((metricGroup != null) && (metricGroup.getName() != null)) {
                htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(metricGroup.getName())).append("\"");
            }
        }

        htmlBody.append(">\n</div>\n");


        // alert type
        htmlBody.append("<div class=\"form-group\">\n");
                
        htmlBody.append("<label class=\"label_small_margin\">Alert type:&nbsp;&nbsp;</label>\n");
        
        htmlBody.append("<input type=\"radio\" id=\"Type_Availability\" name=\"Type\" value=\"Availability\" ");
        if ((alert != null) && (alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_AVAILABILITY)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Availability &nbsp;&nbsp;&nbsp;\n");
        
        htmlBody.append("<input type=\"radio\" id=\"Type_Threshold\" name=\"Type\" value=\"Threshold\" ");
        if ((alert != null) && (alert.getAlertType() != null) && (alert.getAlertType() == Alert.TYPE_THRESHOLD)) htmlBody.append(" checked=\"checked\"");
        htmlBody.append("> Threshold\n");

        htmlBody.append("</div>");
        
        
        // is enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is alert enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"Enabled\" id=\"Enabled\" type=\"checkbox\" ");

        if (((alert != null) && (alert.isEnabled() != null) && alert.isEnabled()) || 
                (alert == null) || (alert.isEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");

        
        // is caution alerting enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is caution alerting enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"CautionEnabled\" id=\"CautionEnabled\" type=\"checkbox\" ");

        if (((alert != null) && (alert.isCautionEnabled() != null) && alert.isCautionEnabled()) || 
                (alert == null) || (alert.isCautionEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // is danger alerting enabled?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Is danger alerting enabled?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"DangerEnabled\" id=\"DangerEnabled\" type=\"checkbox\" ");

        if (((alert != null) && (alert.isDangerEnabled() != null) && alert.isDangerEnabled()) || 
                (alert == null) || (alert.isDangerEnabled() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // alert on positive?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Alert on positive?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"AlertOnPositive\" id=\"AlertOnPositive\" type=\"checkbox\" ");
        
        if (((alert != null) && (alert.isAlertOnPositive() != null) && alert.isAlertOnPositive()) || 
                (alert == null) || (alert.isAlertOnPositive() == null)) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // allow resend alert?
        htmlBody.append(
            "<div class=\"form-group\">\n" +
            "  <label class=\"label_small_margin\">Resend alert?&nbsp;&nbsp;</label>\n" +
            "  <input name=\"AllowResendAlert\" id=\"AllowResendAlert\" type=\"checkbox\" ");
        
        if ((alert != null) && (alert.isAllowResendAlert()!= null) && alert.isAllowResendAlert()) {
            htmlBody.append(" checked=\"checked\"");
        }

        htmlBody.append(">\n</div>\n");
        

        // resend alert every...
        htmlBody.append("<label id=\"ResendAlertEvery_Label\" class=\"label_small_margin\">Resend alert every...</label>\n");
        htmlBody.append("<button type=\"button\" id=\"ResendAlertEvery_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"Specifies how long to wait before resending a notification for a triggered alert.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("<div>\n");
        
        htmlBody.append(
            "<div class=\"form-group col-xs-6\">\n" +
            "  <input class=\"form-control-statsagg\" placeholder=\"If 'resend alert' is enabled, how often should the alert be resent?\" name=\"ResendAlertEvery\" id=\"ResendAlertEvery\" ");

        if ((alert != null) && (alert.getResendAlertEvery() != null)) {
            BigDecimal sendAlertEvery = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getResendAlertEvery(), alert.getResendAlertEveryTimeUnit());
            htmlBody.append(" value=\"").append(sendAlertEvery.stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // resend alert every... time unit
        htmlBody.append(
            "<div class=\"form-group col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"ResendAlertEveryTimeUnit\" id=\"ResendAlertEveryTimeUnit\">\n");

        if ((alert != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getResendAlertEveryTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getResendAlertEveryTimeUnit(), true);
            
            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Seconds</option>\n" +
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");
        htmlBody.append("</div>\n");
        
        // end column 1
        htmlBody.append(
            "    </div>\n" +
            "  </div>\n" +        
            "</div>\n");
        
        
        // start column 2
        htmlBody.append(
            "<div class=\"col-md-4 statsagg_three_panel_second_panel\" id=\"CautionCriteria\" >\n" +
            "  <div class=\"panel panel-warning\">\n" +
            "    <div class=\"panel-heading\"><b>Caution Criteria</b> " +
            "    <a id=\"CautionPreview\" name=\"CautionPreview\" class=\"iframe cboxElement statsagg_caution_preview pull-right\" href=\"#\" onclick=\"generateAlertPreviewLink('Caution');\">Preview</a>" + 
            "    </div>" +
            "    <div class=\"panel-body\">");
        
        
        // warning for when no alert-type is selected
        htmlBody.append("<label id=\"CautionNoAlertTypeSelected_Label\" class=\"label_small_margin\">Please select an alert type</label>\n");

        
        // caution notification group name
        htmlBody.append(
            "<div id=\"CautionNotificationGroupName_Div\">\n" +
            "  <div class=\"form-group\" id=\"CautionNotificationGroupName_Lookup\">\n" +
            "    <label id=\"CautionNotificationGroupName_Label\" class=\"label_small_margin\">Notification group name</label>\n" +
            "    <button type=\"button\" id=\"CautionNotificationGroupName_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the notification group to send alerts to.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"CautionNotificationGroupName\" id=\"CautionNotificationGroupName\" ");

        if ((alert != null) && (alert.getCautionNotificationGroupId() != null)) {
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroup(alert.getCautionNotificationGroupId());

            if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
                htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(notificationGroup.getName())).append("\"");
            }
        }
        
        htmlBody.append(">\n</div></div>\n");
        
        
        // caution positive notification group name
        htmlBody.append(
            "<div id=\"CautionPositiveNotificationGroupName_Div\">\n" +
            "  <div class=\"form-group\" id=\"CautionPositiveNotificationGroupName_Lookup\">\n" +
            "    <label id=\"CautionPositiveNotificationGroupName_Label\" class=\"label_small_margin\">Positive notification group name</label>\n" +
            "    <button type=\"button\" id=\"CautionPositiveNotificationGroupName_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the notification group to send positive alerts to.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"CautionPositiveNotificationGroupName\" id=\"CautionPositiveNotificationGroupName\" ");

        if ((alert != null) && (alert.getCautionPositiveNotificationGroupId() != null)) {
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroup(alert.getCautionPositiveNotificationGroupId());

            if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
                htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(notificationGroup.getName())).append("\"");
            }
        }
        
        htmlBody.append(">\n</div></div>\n");
        
        
        // caution window duration
        htmlBody.append("<div id=\"CautionWindowDuration_Div\">\n");
        htmlBody.append("  <label id=\"CautionWindowDuration_Label\" class=\"label_small_margin\">Window duration</label>\n");
        htmlBody.append("  <button type=\"button\" id=\"CautionWindowDuration_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A rolling time window between 'now' and 'X' time units ago. Values that fall in this window are used in alert evaluation.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("  <div>\n");
        
        htmlBody.append(
            "<div class=\"col-xs-6\" style=\"margin-bottom: 11px;\">\n" +
            "  <input class=\"form-control-statsagg\" name=\"CautionWindowDuration\" id=\"CautionWindowDuration\" ");

        if ((alert != null) && (alert.getCautionWindowDuration() != null)) {
            BigDecimal cautionWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getCautionWindowDuration(), alert.getCautionWindowDurationTimeUnit());
            htmlBody.append(" value=\"").append(cautionWindowDuration.stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // caution window duration time unit
        htmlBody.append(
            "<div class=\"col-xs-6\" style=\"margin-bottom: 11px;\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"CautionWindowDurationTimeUnit\" id=\"CautionWindowDurationTimeUnit\">\n");
        
        if ((alert != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getCautionWindowDurationTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getCautionWindowDurationTimeUnit(), true);
            
            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Seconds</option>\n" +
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div></div></div>\n");
        
        
        // caution stop tracking after
        htmlBody.append("<div id=\"CautionStopTrackingAfter_Div\" >\n");
        htmlBody.append("  <label id=\"CautionStopTrackingAfter_Label\" class=\"label_small_margin\">Stop tracking after...</label>\n");
        htmlBody.append("  <button type=\"button\" id=\"CautionStopTrackingAfter_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"After a metric has not been seen for X time-units, stop alerting on it.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("  <div>\n");
        
        htmlBody.append(   
            "<div class=\"col-xs-6\">\n" +
            "  <input class=\"form-control-statsagg\" name=\"CautionStopTrackingAfter\" id=\"CautionStopTrackingAfter\"");

        if ((alert != null) && (alert.getCautionStopTrackingAfter() != null)) {
            BigDecimal cautionStopTrackingAfter = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getCautionStopTrackingAfter(), alert.getCautionStopTrackingAfterTimeUnit());
            htmlBody.append(" value=\"").append(cautionStopTrackingAfter.stripTrailingZeros().toPlainString()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // caution 'stop tracking after' time unit
        htmlBody.append(
            "<div class=\"col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"CautionStopTrackingAfterTimeUnit\" id=\"CautionStopTrackingAfterTimeUnit\">\n");

        if ((alert != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getCautionStopTrackingAfterTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getCautionStopTrackingAfterTimeUnit(), true);

            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Seconds</option>\n" +
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div></div></div>\n");
        
        
        // caution minimum sample count
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionMinimumSampleCount_Div\" >\n" +
            "  <label id=\"CautionMinimumSampleCount_Label\" class=\"label_small_margin\">Minimum sample count</label>\n" +
            "  <button type=\"button\" id=\"CautionMinimumSampleCount_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"An alert can only be triggered if there are at least 'X' samples within specified the 'alert window duration'.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"CautionMinimumSampleCount\" id=\"CautionMinimumSampleCount\"");

        if ((alert != null) && (alert.getCautionMinimumSampleCount() != null)) {
            htmlBody.append(" value=\"").append(alert.getCautionMinimumSampleCount()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // caution operator
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionOperator_Div\" >\n" +
            "  <label id=\"CautionOperator_Label\" class=\"label_small_margin\">Operator</label>\n" +
            "  <button type=\"button\" id=\"CautionOperator_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The values of a metric-key are considered for threshold-based alerting when they are above/below/equal-to a certain threshold. This value controls the above/below/equal-to aspect of the alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <select class=\"form-control-statsagg\" name=\"CautionOperator\" id=\"CautionOperator\">\n");

        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase(">")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">&nbsp;&nbsp;(greater than)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase(">=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">=&nbsp;&nbsp;(greater than or equal to)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase("<")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<&nbsp;&nbsp;(less than)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase("<=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<=&nbsp;&nbsp;(less than or equal to)</option>\n");

        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.CAUTION, true, false) != null) && alert.getOperatorString(Alert.CAUTION, true, false).equalsIgnoreCase("=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("=&nbsp;&nbsp;(equal to)</option>\n");

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");     
        
        
        // caution combination
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionCombination_Div\" >\n" +
            "  <label id=\"CautionCombination_Label\" class=\"label_small_margin\">Combination</label>\n" +
            "  <button type=\"button\" id=\"CautionCombination_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"For any metric values that fall within the 'alert window duration', what condition will cause the alert to be triggered? Is the average of the metric values above or below the threshold? Are all metrics values above or below the threshold? Is any metric value above or below the threshold? Are 'at least' or 'at most' X metric values above or below the threshold?\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <select class=\"form-control-statsagg\" name=\"CautionCombination\" id=\"CautionCombination\">\n");

        if ((alert != null) && (alert.getCombinationString(Alert.CAUTION) != null)) {
            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("Any")) htmlBody.append("<option selected=\"selected\">Any</option>\n");
            else htmlBody.append("<option>Any</option>\n");

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("All")) htmlBody.append("<option selected=\"selected\">All</option>\n");
            else htmlBody.append("<option>All</option>\n");

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("Average")) htmlBody.append("<option selected=\"selected\">Average</option>\n");
            else htmlBody.append("<option>Average</option>\n");

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("At most")) htmlBody.append("<option selected=\"selected\">At most</option>\n");
            else htmlBody.append("<option>At most</option>\n");

            if (alert.getCombinationString(Alert.CAUTION).equalsIgnoreCase("At least")) htmlBody.append("<option selected=\"selected\">At least</option>\n");
            else htmlBody.append("<option>At least</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Any</option>\n" +
                "<option>All</option>\n" +
                "<option>Average</option>\n" +
                "<option>At most</option>\n" +
                "<option>At least</option>\n"
            );
        }

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");        
        
        
        // caution combination count
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionCombinationCount_Div\" >\n" +
            "  <label id=\"CautionCombinationCount_Label\" class=\"label_small_margin\">Combination count</label>\n" +
            "  <button type=\"button\" id=\"CautionCombination_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"If using a combination of 'at most' or 'at least', then you must specify a count. This refers to the number of independent metric values for a single metric-key that fall within the 'alert window duration'.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"CautionCombinationCount\" id=\"CautionCombinationCount\" ");

        if ((alert != null) && (alert.getCautionCombinationCount() != null)) {
            htmlBody.append(" value=\"").append(alert.getCautionCombinationCount()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        
        // caution threshold
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"CautionThreshold_Div\" >\n" +
            "  <label id=\"CautionThreshold_Label\" class=\"label_small_margin\">Threshold</label>\n" +
            "  <button type=\"button\" id=\"CautionThreshold_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The numeric threshold that, if crossed, will trigger the alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"CautionThreshold\" id=\"CautionThreshold\" ");

        if ((alert != null) && (alert.getCautionThreshold() != null)) {
            htmlBody.append(" value=\"").append(alert.getCautionThreshold().stripTrailingZeros().toPlainString()).append("\"");
        }
                
        htmlBody.append(">\n</div>\n");
        
        // end column 2
        htmlBody.append("</div>\n</div>\n</div>\n");

               
        // start column 3
        htmlBody.append(
            "<div class=\"col-md-4 statsagg_three_panel_second_panel\" id=\"DangerCriteria\" >\n" +
            "  <div class=\"panel panel-danger\">\n" +
            "    <div class=\"panel-heading\"><b>Danger Criteria</b> " +
            "    <a id=\"DangerPreview\" name=\"DangerPreview\" class=\"iframe cboxElement statsagg_danger_preview pull-right\" href=\"#\" onclick=\"generateAlertPreviewLink('Danger');\">Preview</a>" + 
            "    </div>" +
            "    <div class=\"panel-body\">");
        
        
        // warning for when no alert-type is selected
        htmlBody.append("<label id=\"DangerNoAlertTypeSelected_Label\" class=\"label_small_margin\">Please select an alert type</label>\n");

        
        // danger notification group name
        htmlBody.append(
            "<div id=\"DangerNotificationGroupName_Div\">\n" +
            "  <div class=\"form-group\" id=\"DangerNotificationGroupName_Lookup\">\n" +
            "    <label id=\"DangerNotificationGroupName_Label\" class=\"label_small_margin\">Notification group name</label>\n" +
            "    <button type=\"button\" id=\"DangerNotificationGroupName_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the notification group to send alerts to.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"DangerNotificationGroupName\" id=\"DangerNotificationGroupName\" ");

        if ((alert != null) && (alert.getDangerNotificationGroupId() != null)) {
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroup(alert.getDangerNotificationGroupId());

            if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
                htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(notificationGroup.getName())).append("\"");
            }
        }
        
        htmlBody.append(">\n</div></div>\n");
        
        
        // danger positive notification group name
        htmlBody.append(
            "<div id=\"DangerPositiveNotificationGroupName_Div\">\n" +
            "  <div class=\"form-group\" id=\"DangerPositiveNotificationGroupName_Lookup\">\n" +
            "    <label id=\"DangerPositiveNotificationGroupName_Label\" class=\"label_small_margin\">Positive notification group name</label>\n" +
            "    <button type=\"button\" id=\"DangerPositiveNotificationGroupName_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The exact name of the notification group to send positive alerts to.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "    <input class=\"typeahead form-control-statsagg\" autocomplete=\"off\" name=\"DangerPositiveNotificationGroupName\" id=\"DangerPositiveNotificationGroupName\" ");

        if ((alert != null) && (alert.getDangerPositiveNotificationGroupId() != null)) {
            NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
            NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroup(alert.getDangerPositiveNotificationGroupId());

            if ((notificationGroup != null) && (notificationGroup.getName() != null)) {
                htmlBody.append(" value=\"").append(Encode.forHtmlAttribute(notificationGroup.getName())).append("\"");
            }
        }
        
        htmlBody.append(">\n</div></div>\n");
        
        
        // danger window duration
        htmlBody.append("<div id=\"DangerWindowDuration_Div\">\n");
        htmlBody.append("  <label id=\"DangerWindowDuration_Label\" class=\"label_small_margin\">Window duration</label>\n");
        htmlBody.append("  <button type=\"button\" id=\"DangerWindowDuration_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"A rolling time window between 'now' and 'X' time units ago. Values that fall in this window are used in alert evaluation.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("  <div>\n");
        
        htmlBody.append(
            "<div class=\"col-xs-6\" style=\"margin-bottom: 11px;\">\n" +
            "  <input class=\"form-control-statsagg\" name=\"DangerWindowDuration\" id=\"DangerWindowDuration\" ");

        if ((alert != null) && (alert.getDangerWindowDuration() != null)) {
            BigDecimal dangerWindowDuration = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getDangerWindowDuration(), alert.getDangerWindowDurationTimeUnit());
            htmlBody.append(" value=\"").append(dangerWindowDuration.stripTrailingZeros().toPlainString()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");
        
        
        // danger window duration time unit
        htmlBody.append(
            "<div class=\"col-xs-6\" style=\"margin-bottom: 11px;\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"DangerWindowDurationTimeUnit\" id=\"DangerWindowDurationTimeUnit\">\n");
        
        if ((alert != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getDangerWindowDurationTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getDangerWindowDurationTimeUnit(), true);
            
            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Seconds</option>\n" +
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div></div></div>\n");
        
        
        // danger stop tracking after
        htmlBody.append("<div id=\"DangerStopTrackingAfter_Div\" >\n");
        htmlBody.append("  <label id=\"DangerStopTrackingAfter_Label\" class=\"label_small_margin\">Stop tracking after...</label>\n");
        htmlBody.append("  <button type=\"button\" id=\"DangerStopTrackingAfter_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"After a metric has not been seen for X time-units, stop alerting on it.\" style=\"margin-bottom: 1.5px;\">?</button> ");
        htmlBody.append("  <div>\n");
        
        htmlBody.append(   
            "<div class=\"col-xs-6\">\n" +
            "  <input class=\"form-control-statsagg\" name=\"DangerStopTrackingAfter\" id=\"DangerStopTrackingAfter\"");

        if ((alert != null) && (alert.getDangerStopTrackingAfter() != null)) {
            BigDecimal dangerStopTrackingAfter = DatabaseObjectCommon.getValueForTimeFromMilliseconds(alert.getDangerStopTrackingAfter(), alert.getDangerStopTrackingAfterTimeUnit());
            htmlBody.append(" value=\"").append(dangerStopTrackingAfter.stripTrailingZeros().toPlainString()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // danger 'stop tracking after' time unit
        htmlBody.append(
            "<div class=\"col-xs-6\">\n" +
            "  <select class=\"form-control-statsagg\" name=\"DangerStopTrackingAfterTimeUnit\" id=\"DangerStopTrackingAfterTimeUnit\">\n");

        if ((alert != null) && (DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getDangerStopTrackingAfterTimeUnit(), true) != null)) {
            String timeUnitString = DatabaseObjectCommon.getTimeUnitStringFromCode(alert.getDangerStopTrackingAfterTimeUnit(), true);

            if (timeUnitString.equalsIgnoreCase("Seconds")) htmlBody.append("<option selected=\"selected\">Seconds</option>\n");
            else htmlBody.append("<option>Seconds</option>\n");

            if (timeUnitString.equalsIgnoreCase("Minutes")) htmlBody.append("<option selected=\"selected\">Minutes</option>\n");
            else htmlBody.append("<option>Minutes</option>\n");

            if (timeUnitString.equalsIgnoreCase("Hours")) htmlBody.append("<option selected=\"selected\">Hours</option>\n");
            else htmlBody.append("<option>Hours</option>\n");

            if (timeUnitString.equalsIgnoreCase("Days")) htmlBody.append("<option selected=\"selected\">Days</option>\n");
            else htmlBody.append("<option>Days</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Seconds</option>\n" +
                "<option>Minutes</option>\n" +
                "<option>Hours</option>\n" +
                "<option>Days</option>\n"
            );
        }
        
        htmlBody.append("</select>\n");
        htmlBody.append("</div></div></div>\n");
        
        
        // danger minimum sample count
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerMinimumSampleCount_Div\" >\n" +
            "  <label id=\"DangerMinimumSampleCount_Label\" class=\"label_small_margin\">Minimum sample count</label>\n" +
            "  <button type=\"button\" id=\"DangerMinimumSampleCount_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"An alert can only be triggered if there are at least 'X' samples within specified the 'alert window duration'.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"DangerMinimumSampleCount\" id=\"DangerMinimumSampleCount\"");

        if ((alert != null) && (alert.getDangerMinimumSampleCount() != null)) {
            htmlBody.append(" value=\"").append(alert.getDangerMinimumSampleCount()).append("\"");
        }

        htmlBody.append(">\n</div>\n");
        
        
        // danger operator
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerOperator_Div\" >\n" +
            "  <label id=\"DangerOperator_Label\" class=\"label_small_margin\">Operator</label>\n" +
            "  <button type=\"button\" id=\"DangerOperator_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The values of a metric-key are considered for threshold-based alerting when they are above/below/equal-to a certain threshold. This value controls the above/below/equal-to aspect of the alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <select class=\"form-control-statsagg\" name=\"DangerOperator\" id=\"DangerOperator\">\n");

        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase(">")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">&nbsp;&nbsp;(greater than)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase(">=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append(">=&nbsp;&nbsp;(greater than or equal to)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase("<")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<&nbsp;&nbsp;(less than)</option>\n");
        
        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase("<=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("<=&nbsp;&nbsp;(less than or equal to)</option>\n");

        htmlBody.append("<option");
        if ((alert != null) && (alert.getOperatorString(Alert.DANGER, true, false) != null) && alert.getOperatorString(Alert.DANGER, true, false).equalsIgnoreCase("=")) htmlBody.append(" selected=\"selected\">");
        else htmlBody.append(">");
        htmlBody.append("=&nbsp;&nbsp;(equal to)</option>\n");

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");     
        
        
        // danger combination
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerCombination_Div\" >\n" +
            "  <label id=\"DangerCombination_Label\" class=\"label_small_margin\">Combination</label>\n" +
            "  <button type=\"button\" id=\"DangerCombination_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"For any metric values that fall within the 'alert window duration', what condition will cause the alert to be triggered? Is the average of the metric values above or below the threshold? Are all metrics values above or below the threshold? Is any metric value above or below the threshold? Are 'at least' or 'at most' X metric values above or below the threshold?\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <select class=\"form-control-statsagg\" name=\"DangerCombination\" id=\"DangerCombination\">\n");

        if ((alert != null) && (alert.getCombinationString(Alert.DANGER) != null)) {
            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("Any")) htmlBody.append("<option selected=\"selected\">Any</option>\n");
            else htmlBody.append("<option>Any</option>\n");

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("All")) htmlBody.append("<option selected=\"selected\">All</option>\n");
            else htmlBody.append("<option>All</option>\n");

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("Average")) htmlBody.append("<option selected=\"selected\">Average</option>\n");
            else htmlBody.append("<option>Average</option>\n");

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("At most")) htmlBody.append("<option selected=\"selected\">At most</option>\n");
            else htmlBody.append("<option>At most</option>\n");

            if (alert.getCombinationString(Alert.DANGER).equalsIgnoreCase("At least")) htmlBody.append("<option selected=\"selected\">At least</option>\n");
            else htmlBody.append("<option>At least</option>\n");
        }
        else {
            htmlBody.append(
                "<option>Any</option>\n" +
                "<option>All</option>\n" +
                "<option>Average</option>\n" +
                "<option>At most</option>\n" +
                "<option>At least</option>\n"
            );
        }

        htmlBody.append("</select>\n");
        htmlBody.append("</div>\n");        
        
        
        // danger combination count
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerCombinationCount_Div\" >\n" +
            "  <label id=\"DangerCombinationCount_Label\" class=\"label_small_margin\">Combination count</label>\n" +
            "  <button type=\"button\" id=\"DangerCombination_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"If using a combination of 'at most' or 'at least', then you must specify a count. This refers to the number of independent metric values for a single metric-key that fall within the 'alert window duration'.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"DangerCombinationCount\" id=\"DangerCombinationCount\" ");

        if ((alert != null) && (alert.getDangerCombinationCount() != null)) {
            htmlBody.append(" value=\"").append(alert.getDangerCombinationCount()).append("\"");
        }
        
        htmlBody.append(">\n</div>\n");

        
        // danger threshold
        htmlBody.append(
            "<div class=\"form-group statsagg_typeahead_form_margin_correction\" id=\"DangerThreshold_Div\" >\n" +
            "  <label id=\"DangerThreshold_Label\" class=\"label_small_margin\">Threshold</label>\n" +
            "  <button type=\"button\" id=\"DangerThreshold_Help\" class=\"btn btn-xs btn-circle btn-info pull-right\" data-toggle=\"popover\" data-placement=\"left\" data-content=\"The numeric threshold that, if crossed, will trigger the alert.\" style=\"margin-bottom: 1.5px;\">?</button> " + 
            "  <input class=\"form-control-statsagg\" name=\"DangerThreshold\" id=\"DangerThreshold\" ");

        if ((alert != null) && (alert.getDangerThreshold() != null)) {
            htmlBody.append(" value=\"").append(alert.getDangerThreshold().stripTrailingZeros().toPlainString()).append("\"");
        }
                
        htmlBody.append(">\n</div>\n");
        
        
        // end column 3 & form
        htmlBody.append(             
            "      </div>\n" +
            "    </div>\n" +
            "  </div>\n" + 
            "</div>\n" +
            "<button type=\"submit\" class=\"btn btn-default btn-primary statsagg_button_no_shadow statsagg_page_content_font\">Submit</button>" +
            "&nbsp;&nbsp;&nbsp;" +
            "<a href=\"Alerts\" class=\"btn btn-default statsagg_page_content_font\" role=\"button\">Cancel</a>" +
            "</form>\n" +
            "</div>\n" +
            "</div>\n"
            );

        return htmlBody.toString();
    }

    public String parseAndAlterAlert(Object request) {
        
        if (request == null) {
            return null;
        }
        
        String returnString;
        
        Alert alert = getAlertFromAlertParameters(request);
        String oldName = Common.getParameterAsString(request, "Old_Name");
        if (oldName == null) oldName = Common.getParameterAsString(request, "old_name");
        if (oldName == null) {
            String id = Common.getParameterAsString(request, "Id");
            if (id == null) id = Common.getParameterAsString(request, "id");
            
            if (id != null) {
                try {
                    Integer id_Integer = Integer.parseInt(id.trim());
                    AlertsDao alertsDao = new AlertsDao();
                    Alert oldAlert = alertsDao.getAlert(id_Integer);
                    oldName = oldAlert.getName();
                }
                catch (Exception e){}
            }
        }
        
        // insert/update/delete records in the database
        if ((alert != null) && (alert.getName() != null)) {
            AlertsLogic alertsLogic = new AlertsLogic();
            returnString = alertsLogic.alterRecordInDatabase(alert, oldName, false);
            
            if ((GlobalVariables.alertInvokerThread != null) && (AlertsLogic.STATUS_CODE_SUCCESS == alertsLogic.getLastAlterRecordStatus())) {
                GlobalVariables.alertInvokerThread.runAlertThread(false, true);
            }
        }
        else {
            returnString = "Failed to add alert. Reason=\"Field validation failed.\"";
            logger.warn(returnString);
        }
        
        return returnString;
    }
    
    public static Alert getAlertFromAlertParameters(Object request) {
        
        if (request == null) {
            return null;
        }
        
        boolean didEncounterError = false;
        
        Alert alert = new Alert();

        try {
            String parameter;

            parameter = Common.getParameterAsString(request, "Name");
            if (parameter == null) parameter = Common.getParameterAsString(request, "name");
            String trimmedName = parameter.trim();
            alert.setName(trimmedName);
            alert.setUppercaseName(trimmedName.toUpperCase());
            if ((alert.getName() == null) || alert.getName().isEmpty()) didEncounterError = true;
            
            parameter = Common.getParameterAsString(request, "Description");
            if (parameter == null) parameter = Common.getParameterAsString(request, "description");
            if (parameter != null) {
                String trimmedParameter = parameter.trim();
                String description;
                if (trimmedParameter.length() > 100000) description = trimmedParameter.substring(0, 99999);
                else description = trimmedParameter;
                alert.setDescription(description);
            }
            else alert.setDescription("");
            
            parameter = Common.getParameterAsString(request, "MetricGroupName");
            if (parameter == null) parameter = Common.getParameterAsString(request, "metric_group_name");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    MetricGroupsDao metricGroupsDao = new MetricGroupsDao();
                    MetricGroup metricGroup = metricGroupsDao.getMetricGroupByName(parameterTrimmed);
                    if (metricGroup != null) alert.setMetricGroupId(metricGroup.getId());
                }
            }
            else {
                parameter = Common.getParameterAsString(request, "MetricGroupId");
                if (parameter == null) parameter = Common.getParameterAsString(request, "metric_group_id");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty()) alert.setMetricGroupId(Integer.parseInt(parameterTrimmed));
                }
            }

            parameter = Common.getParameterAsString(request, "Enabled");
            if (parameter == null) parameter = Common.getParameterAsString(request, "enabled");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alert.setIsEnabled(true);
            else alert.setIsEnabled(false);

            parameter = Common.getParameterAsString(request, "CautionEnabled");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_enabled");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alert.setIsCautionEnabled(true);
            else alert.setIsCautionEnabled(false);
            
            parameter = Common.getParameterAsString(request, "DangerEnabled");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_enabled");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alert.setIsDangerEnabled(true);
            else alert.setIsDangerEnabled(false);
            
            parameter = Common.getParameterAsString(request, "Type");
            if (parameter == null) parameter = Common.getParameterAsString(request, "alert_type");
            if ((parameter != null) && parameter.contains("Availability")) alert.setAlertType(Alert.TYPE_AVAILABILITY);
            else if ((parameter != null) && parameter.contains("Threshold")) alert.setAlertType(Alert.TYPE_THRESHOLD);
            
            parameter = Common.getParameterAsString(request, "AlertOnPositive");
            if (parameter == null) parameter = Common.getParameterAsString(request, "alert_on_positive");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alert.setAlertOnPositive(true);
            else alert.setAlertOnPositive(false);

            parameter = Common.getParameterAsString(request, "AllowResendAlert");
            if (parameter == null) parameter = Common.getParameterAsString(request, "allow_resend_alert");
            if ((parameter != null) && (parameter.contains("on") || parameter.equalsIgnoreCase("true"))) alert.setAllowResendAlert(true);
            else alert.setAllowResendAlert(false);

            parameter = Common.getParameterAsString(request, "ResendAlertEveryTimeUnit");
            if (parameter == null) parameter = Common.getParameterAsString(request, "resend_alert_every_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setResendAlertEveryTimeUnit(intValue);
                }
            }
            
            parameter = Common.getParameterAsString(request, "ResendAlertEvery");
            if (parameter == null) parameter = Common.getParameterAsString(request, "resend_alert_every");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alert.getResendAlertEveryTimeUnit());
                    if (timeInMs != null) alert.setResendAlertEvery(timeInMs.longValue());                    
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionNotificationGroupName");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_notification_group_name");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setCautionNotificationGroupId(notificationGroup.getId());
                }
            }
            else {
                parameter = Common.getParameterAsString(request, "CautionNotificationGroupId");
                if (parameter == null) parameter = Common.getParameterAsString(request, "caution_notification_group_id");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty()) alert.setCautionNotificationGroupId(Integer.parseInt(parameterTrimmed));
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionPositiveNotificationGroupName");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_positive_notification_group_name");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setCautionPositiveNotificationGroupId(notificationGroup.getId());
                }
            }
            else {
                parameter = Common.getParameterAsString(request, "CautionPositiveNotificationGroupId");
                if (parameter == null) parameter = Common.getParameterAsString(request, "caution_positive_notification_group_id");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty()) alert.setCautionPositiveNotificationGroupId(Integer.parseInt(parameterTrimmed));
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionWindowDurationTimeUnit");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_window_duration_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setCautionWindowDurationTimeUnit(intValue);
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionWindowDuration");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_window_duration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alert.getCautionWindowDurationTimeUnit());
                    if (timeInMs != null) alert.setCautionWindowDuration(timeInMs.longValue());                    
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionStopTrackingAfterTimeUnit");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_stop_tracking_after_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setCautionStopTrackingAfterTimeUnit(intValue);
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionStopTrackingAfter");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_stop_tracking_after");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {     
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alert.getCautionStopTrackingAfterTimeUnit());
                    if (timeInMs != null) alert.setCautionStopTrackingAfter(timeInMs.longValue());
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionMinimumSampleCount");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_minimum_sample_count");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setCautionMinimumSampleCount(intValue);
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionOperator");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_operator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alert.setCautionOperator(intValue);
                }
            }

            parameter = Common.getParameterAsString(request, "CautionCombination");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_combination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = Alert.getCombinationCodeFromString(parameterTrimmed);
                    alert.setCautionCombination(intValue);                
                }
            }
            
            parameter = Common.getParameterAsString(request, "CautionCombinationCount");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_combination_count");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {                    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setCautionCombinationCount(intValue);
                }
            }

            parameter = Common.getParameterAsString(request, "CautionThreshold");
            if (parameter == null) parameter = Common.getParameterAsString(request, "caution_threshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alert.setCautionThreshold(bigDecimalValue);
                }
            }

            parameter = Common.getParameterAsString(request, "DangerNotificationGroupName");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_notification_group_name");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setDangerNotificationGroupId(notificationGroup.getId());
                }
            }
            else {
                parameter = Common.getParameterAsString(request, "DangerNotificationGroupId");
                if (parameter == null) parameter = Common.getParameterAsString(request, "danger_notification_group_id");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty()) alert.setDangerNotificationGroupId(Integer.parseInt(parameterTrimmed));
                }
            }
            
            parameter = Common.getParameterAsString(request, "DangerPositiveNotificationGroupName");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_positive_notification_group_name");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    NotificationGroupsDao notificationGroupsDao = new NotificationGroupsDao();
                    NotificationGroup notificationGroup = notificationGroupsDao.getNotificationGroupByName(parameterTrimmed);
                    if ((notificationGroup != null) && (notificationGroup.getId() != null)) alert.setDangerPositiveNotificationGroupId(notificationGroup.getId());
                }
            }
            else {
                parameter = Common.getParameterAsString(request, "DangerPositiveNotificationGroupId");
                if (parameter == null) parameter = Common.getParameterAsString(request, "danger_positive_notification_group_id");
                if (parameter != null) {
                    String parameterTrimmed = parameter.trim();
                    if (!parameterTrimmed.isEmpty()) alert.setDangerPositiveNotificationGroupId(Integer.parseInt(parameterTrimmed));
                }
            }
            
            parameter = Common.getParameterAsString(request, "DangerWindowDurationTimeUnit");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_window_duration_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setDangerWindowDurationTimeUnit(intValue);
                }
            }
            
            parameter = Common.getParameterAsString(request, "DangerWindowDuration");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_window_duration");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alert.getDangerWindowDurationTimeUnit());
                    if (timeInMs != null) alert.setDangerWindowDuration(timeInMs.longValue());
                }
            }
            
            parameter = Common.getParameterAsString(request, "DangerStopTrackingAfterTimeUnit");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_stop_tracking_after_time_unit");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {      
                    Integer intValue = DatabaseObjectCommon.getTimeUnitCodeFromString(parameterTrimmed);
                    alert.setDangerStopTrackingAfterTimeUnit(intValue);
                }
            }
            
            parameter = Common.getParameterAsString(request, "DangerStopTrackingAfter");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_stop_tracking_after");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal time = new BigDecimal(parameterTrimmed, DatabaseObjectCommon.TIME_UNIT_MATH_CONTEXT);
                    BigDecimal timeInMs = DatabaseObjectCommon.getMillisecondValueForTime(time, alert.getDangerStopTrackingAfterTimeUnit());
                    if (timeInMs != null) alert.setDangerStopTrackingAfter(timeInMs.longValue());
                }
            }
           
            parameter = Common.getParameterAsString(request, "DangerMinimumSampleCount");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_minimum_sample_count");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setDangerMinimumSampleCount(intValue);
                }
            }
            
            parameter = Common.getParameterAsString(request, "DangerOperator");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_operator");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Alert.getOperatorCodeFromOperatorString(parameterTrimmed);
                    alert.setDangerOperator(intValue);
                }
            }

            parameter = Common.getParameterAsString(request, "DangerCombination");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_combination");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Alert.getCombinationCodeFromString(parameterTrimmed);
                    alert.setDangerCombination(intValue);
                }
            }
            
            parameter = Common.getParameterAsString(request, "DangerCombinationCount");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_combination_count");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    Integer intValue = Integer.parseInt(parameterTrimmed);
                    alert.setDangerCombinationCount(intValue);
                }
            }

            parameter = Common.getParameterAsString(request, "DangerThreshold");
            if (parameter == null) parameter = Common.getParameterAsString(request, "danger_threshold");
            if (parameter != null) {
                String parameterTrimmed = parameter.trim();
                if (!parameterTrimmed.isEmpty()) {    
                    BigDecimal bigDecimalValue = new BigDecimal(parameterTrimmed);
                    alert.setDangerThreshold(bigDecimalValue);
                }
            }
        }
        catch (Exception e) {
            didEncounterError = true;
            logger.error(e.toString() + System.lineSeparator() + StackTrace.getStringFromStackTrace(e));
        }
            
        if (!didEncounterError) {
            alert.setIsCautionAlertActive(false);
            alert.setCautionFirstActiveAt(null);
            alert.setIsCautionAlertAcknowledged(null);
            alert.setCautionAlertLastSentTimestamp(null);
            alert.setCautionActiveAlertsSet(null);
            alert.setIsDangerAlertActive(false);
            alert.setDangerFirstActiveAt(null);
            alert.setIsDangerAlertAcknowledged(null);
            alert.setDangerAlertLastSentTimestamp(null);
            alert.setDangerActiveAlertsSet(null);
        }
        else {
            alert = null;
        }
        
        return alert;
    }
    
}
