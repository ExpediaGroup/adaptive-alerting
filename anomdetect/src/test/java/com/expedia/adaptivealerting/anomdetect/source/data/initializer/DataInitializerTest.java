package com.expedia.adaptivealerting.anomdetect.source.data.initializer;

import com.expedia.adaptivealerting.anomdetect.detect.AnomalyType;
import com.expedia.adaptivealerting.anomdetect.detect.Detector;
import com.expedia.adaptivealerting.anomdetect.detect.MappedMetricData;
import com.expedia.adaptivealerting.anomdetect.detect.outlier.algo.forecasting.ForecastingDetector;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.multiplicative.MultiplicativeIntervalForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.interval.algo.multiplicative.MultiplicativeIntervalForecasterParams;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecaster;
import com.expedia.adaptivealerting.anomdetect.forecast.point.algo.seasonalnaive.SeasonalNaivePointForecasterParams;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSource;
import com.expedia.adaptivealerting.anomdetect.source.data.DataSourceResult;;
import com.expedia.metrics.MetricData;
import com.expedia.metrics.MetricDefinition;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;

public class DataInitializerTest {

    @Mock
    private DataSource dataSource;

    private DataInitializer initializerUnderTest;
    private Detector detector;
    private MappedMetricData mappedMetricData;
    private List<DataSourceResult> dataSourceResults;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        initTestObjects();
        initDependencies();
        this.initializerUnderTest = new DataInitializer(dataSource);
    }

    @Test
    public void testInitializeDetector() {
        initializerUnderTest.initializeDetector(mappedMetricData, detector);
    }

    public void initTestObjects() {
        this.mappedMetricData = buildMappedMetricData();
        this.detector = buildDetector();
        this.dataSourceResults = new ArrayList<>();
        dataSourceResults.add(buildDataSourceResult(1.0, 1578307488));
        dataSourceResults.add(buildDataSourceResult(3.0, 1578307489));
    }

    private void initDependencies() {
        when(dataSource.getMetricData("7d", 2016, "metric-definition")).thenReturn(dataSourceResults);
    }

    private DataSourceResult buildDataSourceResult(Double value, long epochSecs) {
        val dataSourceResult = new DataSourceResult();
        dataSourceResult.setDataPoint(value);
        dataSourceResult.setEpochSecond(epochSecs);
        return dataSourceResult;
    }

    private MappedMetricData buildMappedMetricData() {
        val mappedUuid = UUID.randomUUID();
        val metricDefinition = new MetricDefinition("metric-definition");
        val metricData = new MetricData(metricDefinition, 100.0, Instant.now().getEpochSecond());
        return new MappedMetricData(metricData, mappedUuid);
    }

    private Detector buildDetector() {
        val seasonalNaivePointForecaster = new SeasonalNaivePointForecaster(new SeasonalNaivePointForecasterParams().setCycleLength(22).setIntervalLength(11));
        val multiplicativeIntervalForecaster = new MultiplicativeIntervalForecaster(new MultiplicativeIntervalForecasterParams().setStrongMultiplier(3.0).setWeakMultiplier(1.0));
        return new ForecastingDetector(UUID.randomUUID(), seasonalNaivePointForecaster, multiplicativeIntervalForecaster, AnomalyType.TWO_TAILED, true, "seasonalnaive");
    }
}
