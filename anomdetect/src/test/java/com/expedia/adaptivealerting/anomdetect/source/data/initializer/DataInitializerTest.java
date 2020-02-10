package com.expedia.adaptivealerting.anomdetect.source.data.initializer;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.multiplicative.MultiplicativeIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.multiplicative.MultiplicativeIntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.pewma.PewmaPointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.pewma.PewmaPointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSource;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;
import com.expedia.adaptivealerting.anomdetect.source.data.graphite.GraphiteClient;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import com.typesafe.config.Config;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class DataInitializerTest {

    private DataInitializer initializerUnderTest;

    @Mock
    private DataSource dataSource;

    @Mock
    private Config config;

    @Mock
    private GraphiteClient graphiteClient;

    private Detector detector;
    private MappedMetricData mappedMetricData;
    private List<DataSourceResult> dataSourceResults;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initConfig();
        this.initializerUnderTest = spy(new DataInitializer(config));
        initTestObjects();
        initDependencies();
    }

    @Test
    public void testInitializeDetector() {
        initializerUnderTest.initializeDetector(mappedMetricData, detector);
        verify(initializerUnderTest, atMost(1)).initializeDetector(any(MappedMetricData.class), any(Detector.class));
    }

    @Test(expected = RuntimeException.class)
    public void testInitializeDetector_illegal_point_forecaster() {
        val pwmaPointForecaster = new PewmaPointForecaster(new PewmaPointForecasterParams().setAlpha(1.0).setBeta(2.0));
        val multiplicativeIntervalForecaster = new MultiplicativeIntervalForecaster(new MultiplicativeIntervalForecasterParams().setStrongMultiplier(3.0).setWeakMultiplier(1.0));
        val detector = new ForecastingDetector(UUID.randomUUID(), pwmaPointForecaster, multiplicativeIntervalForecaster, AnomalyType.TWO_TAILED, true, "seasonalnaive");
        initializerUnderTest.initializeDetector(mappedMetricData, detector);
        verify(initializerUnderTest, atMost(1)).initializeDetector(any(MappedMetricData.class), any(Detector.class));
    }

    private void initConfig() {
        when(config.getString(DataInitializer.BASE_URI)).thenReturn("http://graphite");
    }

    public void initTestObjects() {
        this.mappedMetricData = buildMappedMetricData();
        this.detector = buildDetector();
        this.dataSourceResults = new ArrayList<>();
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
    }

    private MappedMetricData buildMappedMetricData() {
        val mappedUuid = UUID.randomUUID();
        val metricDefinition = new MetricDefinition("metric-definition");
        val metricData = new MetricData(metricDefinition, 100.0, Instant.now().getEpochSecond());
        return new MappedMetricData(metricData, mappedUuid);
    }

    private Detector buildDetector() {
        val seasonalNaivePointForecaster = new SeasonalNaivePointForecaster(new SeasonalNaivePointForecasterParams().setCycleLength(2016).setIntervalLength(300));
        val multiplicativeIntervalForecaster = new MultiplicativeIntervalForecaster(new MultiplicativeIntervalForecasterParams().setStrongMultiplier(3.0).setWeakMultiplier(1.0));
        return new ForecastingDetector(UUID.randomUUID(), seasonalNaivePointForecaster, multiplicativeIntervalForecaster, AnomalyType.TWO_TAILED, true, "seasonalnaive");
    }

    private void initDependencies() {
        when(initializerUnderTest.makeClient(anyString())).thenReturn(graphiteClient);
        when(initializerUnderTest.makeSource(graphiteClient)).thenReturn(dataSource);
        when(dataSource.getMetricData(anyLong(), anyLong(), anyInt(), anyString())).thenReturn(dataSourceResults);
    }

    private DataSourceResult buildDataSourceResult(Double value, long epochSecs) {
        val dataSourceResult = new DataSourceResult();
        dataSourceResult.setDataPoint(value);
        dataSourceResult.setEpochSecond(epochSecs);
        return dataSourceResult;
    }
}
