package com.expedia.adaptivealerting.kafka.processor.openmetrics;

import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.expedia.metrics.TagCollection;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class OpenMetricsToMetrics20Mapper {
    private final String TYPE_LINE_PREFIX = "# TYPE";
    private final String HELP_LINE_PREFIX = "# HELP";
    private final String UNIT_LINE_PREFIX = "# UNIT";
    private final static String WORD_REGEX = "[a-zA-Z0-9_:\\-]+";
    private final static String WORD_WITH_LABELS_REGEX = "[a-zA-Z0-9_:\\-]+(\\{.*\\})?";
    private final static String NUMBER_REGEX = "[\\d+\\.?\\d*]+";

    public List<MetricData> convert(final String openMetricsText) {
        val openMetricRecordsList = parse(openMetricsText);
        val metricTypeList = new ArrayList<MetricData>();
        for(val openMetricRecord: openMetricRecordsList) {
            if (openMetricRecord.getTimestamp() != null) {
                metricTypeList.add(convertSingle(openMetricRecord));
            } else {
                log.warn("OpenMetricsToMetrics20Mapper, skipping converting {} to metric2.0, timestamp is empty",
                    openMetricRecord);
            }
        }

        return metricTypeList;
    }

    public List<OpenMetricRecord> parse(String metricsStr) {
        List<OpenMetricRecord> openMetricRecords = new ArrayList<>();
        List<String> logLines = Arrays.asList(metricsStr.split("\\n"));

        String helpStatement = null;
        MetricType metricType = null;
        String metricName = null;
        String metricUnit = null;
        for (String logLine : logLines) {
            try {
                log.debug("OpenMetricsToMetrics20Mapper, parsing OpenMetrics log Line: {}", logLine);
                if (logLine.startsWith(HELP_LINE_PREFIX)) {
                    Pattern p = Pattern.compile(String.format("^(%s)\\s+(.*)$", WORD_REGEX));
                    Matcher m = p.matcher(logLine.replace(HELP_LINE_PREFIX, "").trim());
                    m.find();
                    helpStatement = m.group(2);
                } else if (logLine.startsWith(TYPE_LINE_PREFIX)) {
                    Pattern p = Pattern.compile(String.format("^(%s)\\s+(%s)$", WORD_REGEX, WORD_REGEX));
                    Matcher m = p.matcher(logLine.replace(TYPE_LINE_PREFIX, "").trim());
                    m.find();
                    metricName = m.group(1);
                    metricType = MetricType.getByType(m.group(2));
                } else if (logLine.startsWith(UNIT_LINE_PREFIX)) {
                    Pattern p = Pattern.compile(String.format("^(%s)\\s+(%s)$", WORD_REGEX, WORD_REGEX));
                    Matcher m = p.matcher(logLine.replace(UNIT_LINE_PREFIX, "").trim());
                    m.find();
                    metricUnit = m.group(2);
                } else {
                    String individualMetricLine = logLine;
                    if (individualMetricLine.startsWith(metricName)) {
                        openMetricRecords.add(buildRecordObj(metricName, metricType, metricUnit, helpStatement, logLine));
                    }

                }
            } catch (Exception e) {
                log.error("OpenMetricsToMetrics20Mapper, Error in parsing line: {} Exception: {}", logLine, e);
            }
        }

        return openMetricRecords;
    }

    private MetricData convertSingle(OpenMetricRecord openMetricRecord) {
        Map<String, String> tagsMap = new HashMap<>();
        if (openMetricRecord.getLabelsMap() != null) {
            tagsMap.putAll(openMetricRecord.getLabelsMap());
        }
        if (openMetricRecord.getMetricUnit() != null) {
            tagsMap.put(MetricDefinition.UNIT, openMetricRecord.getMetricUnit());
        }
        tagsMap.put(MetricDefinition.MTYPE, openMetricRecord.getMetricType().getType()); //TODO: revise

        Map<String, String> metaMap = new HashMap<>();

        final TagCollection tagCollection = new TagCollection(tagsMap);
        final TagCollection metaCollection = new TagCollection(metaMap);
        final String key = openMetricRecord.getMetricName()+"_"+openMetricRecord.getSuffix();
        MetricDefinition metricDefinition = new MetricDefinition(key, tagCollection, metaCollection);
        MetricData metricData = new MetricData(metricDefinition,
            openMetricRecord.getValue(),
            openMetricRecord.getTimestamp().longValue());

        return metricData;
    }

    private OpenMetricRecord buildRecordObj(String metricName, MetricType metricType, String metricUnit,
                                            String helpStatement, String logLine) {

        Map<String, String> labelMap = null;
        String suffix = null;
        Double metricValue = null;
        Double metricTimestamp = null;
        Pattern p = Pattern.compile(String.format("^(%s)\\s+(%s)\\s*(%s)?",
            WORD_WITH_LABELS_REGEX, NUMBER_REGEX, NUMBER_REGEX));
        Matcher m = p.matcher(logLine);
        m.find();
        int startLabelPos = logLine.indexOf("{");
        int endLabelPos = logLine.lastIndexOf("}");
        if (startLabelPos != -1 && endLabelPos != -1) {
            labelMap = extractLabelsMap(logLine.substring(startLabelPos + 1, endLabelPos));
        }
        if (m.group(1).startsWith(metricName + "_")) {
            final String identifier = startLabelPos != -1 ? m.group(1).substring(0, startLabelPos) : m.group(1);
            suffix = identifier.replace(metricName + "_", "");
        }

        if (m.group(3) != null) {
            metricValue = Double.parseDouble(m.group(3));
        }

        if (m.group(4) != null) {
            metricTimestamp = Double.parseDouble(m.group(4));
        }

        return OpenMetricRecord.builder().metricName(metricName)
            .metricType(metricType)
            .metricUnit(metricUnit)
            .suffix(suffix)
            .helpDescription(helpStatement)
            .labelsMap(labelMap)
            .value(metricValue)
            .timestamp(metricTimestamp)
            .build();
    }

    private Map<String, String> extractLabelsMap(String labels) {
        Map<String, String> labelsMap = new HashMap<>();
        String labelName = "";
        String labelValue = "";
        boolean valueBlockStarted = false;
        char[] labelsChars = labels.toCharArray();

        for (Character ch : labelsChars) {
            if (valueBlockStarted) {
                if (ch.equals('"')) {
                    if (labelValue.isEmpty()) { // starting "
                        continue;
                    } else if (labelValue.endsWith("\\")) { // double qoutes after escaping character
                        labelValue += ch;
                    } else { // this is double qoute to mark end of the value
                        labelsMap.put(labelName, labelValue);
                        labelName = "";
                        labelValue = "";
                        valueBlockStarted = false;
                    }
                } else {
                    labelValue += ch;
                }
            } else { //handle label
                if (ch.equals('=')) {
                    valueBlockStarted = true;
                } else if (ch.equals(',')) { //comma outside value block is ignored
                    continue;
                } else if (Character.isWhitespace(ch) && labelName.isEmpty()) { // space before label name
                    continue;
                } else {
                    labelName += ch;
                }
            }
        }

        return labelsMap;
    }
}
