package com.linkedin.thirdeye.hadoop.config;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.linkedin.thirdeye.api.MetricType;
import com.linkedin.thirdeye.api.TopKDimensionToMetricsSpec;
import com.linkedin.thirdeye.api.TopkWhitelistSpec;

public class ThirdEyeConfigTest {

  private Properties props;
  private ThirdEyeConfig thirdeyeConfig;
  private ThirdEyeConfig config;

  @BeforeClass
  public void setup() {
    props = new Properties();
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TABLE_NAME.toString(), "collection");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_DIMENSION_NAMES.toString(), "d1,d2,d3");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_METRIC_NAMES.toString(), "m1,m2,m3");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_METRIC_TYPES.toString(), "LONG,FLOAT,INT");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TIMECOLUMN_NAME.toString(), "t1");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TIMECOLUMN_TYPE.toString(), "DAYS");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TIMECOLUMN_SIZE.toString(), "10");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_SPLIT_THRESHOLD.toString(), "1000");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_SPLIT_ORDER.toString(), "d1,d2,d3");

    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_THRESHOLD_METRIC_NAMES.toString(), "m1,m3");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_METRIC_THRESHOLD_VALUES.toString(), "0.02,0.1");

    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_DIMENSION_NAMES.toString(), "d2,d3");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_METRICS.toString() + ".d2", "m1,m2");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_KVALUES.toString() + ".d2", "20,30");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_METRICS.toString() + ".d3", "m1");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_KVALUES.toString() + ".d3", "50");

    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_WHITELIST_DIMENSION_NAMES.toString(), "d1,d2");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_WHITELIST_DIMENSION.toString() + ".d1", "x,y");
    props.setProperty(ThirdEyeConfigConstants.THIRDEYE_WHITELIST_DIMENSION.toString() + ".d2", "a");

    thirdeyeConfig = ThirdEyeConfig.fromProperties(props);

  }



  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testTableNameConfig() throws IllegalArgumentException {
    Assert.assertEquals("collection", thirdeyeConfig.getCollection(), "Collection name not correctly set");
    try {
      props.remove(ThirdEyeConfigConstants.THIRDEYE_TABLE_NAME.toString());
      config = ThirdEyeConfig.fromProperties(props);
    } finally {
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TABLE_NAME.toString(), "collection");
    }
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void testDimensionsConfig() throws IllegalArgumentException {
    Assert.assertEquals(3, thirdeyeConfig.getDimensionNames().size(), "Incorrect number of dimensions");
    Assert.assertEquals(new String[]{"d1", "d2", "d3"}, thirdeyeConfig.getDimensionNames().toArray(), "Incorrect dimensions");

    try {
      props.remove(ThirdEyeConfigConstants.THIRDEYE_DIMENSION_NAMES.toString());
      config = ThirdEyeConfig.fromProperties(props);
    } finally {
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_DIMENSION_NAMES.toString(), "d1,d2,d3");
    }
  }

  @Test
  public void testMetricsConfig() throws IllegalArgumentException {
    boolean failed = false;
    Assert.assertEquals(3, thirdeyeConfig.getMetricNames().size(), "Incorrect number of metrics");
    Assert.assertEquals(3, thirdeyeConfig.getMetrics().size(), "Incorrect number of metric specs");
    Assert.assertEquals(new String[]{"m1", "m2", "m3"}, thirdeyeConfig.getMetricNames().toArray(), "Incorrect metrics");
    MetricType[] actualMetricTypes = new MetricType[3];
    for (int i = 0; i < 3; i++) {
      actualMetricTypes[i] = thirdeyeConfig.getMetrics().get(i).getType();
    }
    Assert.assertEquals(actualMetricTypes, new MetricType[]{MetricType.LONG, MetricType.FLOAT, MetricType.INT}, "Incorrect metric specs");

    try {
      props.remove(ThirdEyeConfigConstants.THIRDEYE_METRIC_NAMES.toString());
      config = ThirdEyeConfig.fromProperties(props);
    } catch (IllegalArgumentException e) {
      failed = true;
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_METRIC_NAMES.toString(), "m1,m2,m3");
    }
    Assert.assertTrue(failed, "Expected exception due to missing metric names property");

    failed = false;
    try {
      props.remove(ThirdEyeConfigConstants.THIRDEYE_METRIC_TYPES.toString());
      config = ThirdEyeConfig.fromProperties(props);
    } catch (IllegalArgumentException e) {
      failed = true;
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_METRIC_TYPES.toString(), "LONG,FLOAT,INT");
    }
    Assert.assertTrue(failed, "Expected exception due to missing metric types property");

    failed = false;
    try {
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_METRIC_NAMES.toString(), "m1,m2");
      config = ThirdEyeConfig.fromProperties(props);
    } catch (IllegalStateException e) {
      failed = true;
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_METRIC_NAMES.toString(), "m1,m2,m3");
    }
    Assert.assertTrue(failed, "Expecetd exception due to inequal number of metric names and types in properties");
  }

  @Test
  public void testTimeConfig() throws IllegalArgumentException {
    boolean failed = false;
    Assert.assertEquals(thirdeyeConfig.getTime().getColumnName(), "t1", "Incorrect time column name");
    Assert.assertEquals(thirdeyeConfig.getTime().getBucket().getSize(), 10, "Incorrect time size");
    Assert.assertEquals(thirdeyeConfig.getTime().getBucket().getUnit(), TimeUnit.DAYS, "Incorrect time unit");

    try {
      props.remove(ThirdEyeConfigConstants.THIRDEYE_TIMECOLUMN_NAME.toString());
      config = ThirdEyeConfig.fromProperties(props);
    } catch (IllegalArgumentException e) {
      failed = true;
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TIMECOLUMN_NAME.toString(), "t1");
    }
    Assert.assertTrue(failed, "Expected exception due to missing time column property");

    props.remove(ThirdEyeConfigConstants.THIRDEYE_TIMECOLUMN_SIZE.toString());
    props.remove(ThirdEyeConfigConstants.THIRDEYE_TIMECOLUMN_TYPE.toString());

    config = ThirdEyeConfig.fromProperties(props);
    Assert.assertEquals(config.getTime().getBucket().getSize(), 1, "Incorrect default time size");
    Assert.assertEquals(config.getTime().getBucket().getUnit(), TimeUnit.HOURS, "Incorrect default time unit");
  }

  @Test
  public void testSplitConfig() throws Exception {
    Assert.assertEquals(thirdeyeConfig.getSplit().getThreshold(), 1000, "Incorrect split threshold");
    Assert.assertEquals(thirdeyeConfig.getSplit().getOrder().toArray(), new String[]{"d1", "d2", "d3"}, "Incorrect split order");

    props.remove(ThirdEyeConfigConstants.THIRDEYE_SPLIT_THRESHOLD.toString());
    config = ThirdEyeConfig.fromProperties(props);
    Assert.assertEquals(config.getSplit(), null, "Default split should be null");
  }

  @Test
  public void testTopKWhitelistConfig() throws IllegalArgumentException {
    boolean failed = false;
    TopkWhitelistSpec topKWhitelistSpec = thirdeyeConfig.getTopKWhitelist();

    // thresholds
    Map<String, Double> threshold = topKWhitelistSpec.getThreshold();
    Assert.assertEquals(threshold.size(), 2, "Incorrect metric thresholds size");
    Assert.assertEquals(threshold.get("m1") == 0.02 && threshold.get("m3") == 0.1, true, "Incorrect metric thresholds config");
    try {
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_METRIC_THRESHOLD_VALUES.toString(), "0.1");
      config = ThirdEyeConfig.fromProperties(props);
    } catch (IllegalStateException e) {
      failed = true;
    }
    Assert.assertTrue(failed, "Expected exception due to unequal number of metrics and threshold");
    props.remove(ThirdEyeConfigConstants.THIRDEYE_TOPK_METRIC_THRESHOLD_VALUES.toString());
    props.remove(ThirdEyeConfigConstants.THIRDEYE_TOPK_THRESHOLD_METRIC_NAMES.toString());
    config = ThirdEyeConfig.fromProperties(props);
    Assert.assertEquals(config.getTopKWhitelist().getThreshold(), null, "Default threshold config should be null");

    // whitelist
    Map<String, String> whitelist = topKWhitelistSpec.getWhitelist();
    Assert.assertEquals(whitelist.size(), 2, "Incorrect size of whitelist dimensions");
    Assert.assertEquals(whitelist.get("d1"), "x,y", "Incorrect whitelist config");
    Assert.assertEquals(whitelist.get("d2"), "a", "Incorrect whitelist config");
    props.remove(ThirdEyeConfigConstants.THIRDEYE_WHITELIST_DIMENSION_NAMES.toString());
    config = ThirdEyeConfig.fromProperties(props);
    Assert.assertEquals(config.getTopKWhitelist().getWhitelist(), null, "Default whitelist config should be null");

    // topk
    List<TopKDimensionToMetricsSpec> topk = topKWhitelistSpec.getTopKDimensionToMetricsSpec();
    Assert.assertEquals(topk.size(), 2, "Incorrect topk dimensions config size");
    TopKDimensionToMetricsSpec topkSpec = topk.get(0);
    Assert.assertEquals(topkSpec.getDimensionName().equals("d2")
          && topkSpec.getTopk().size() == 2
          && topkSpec.getTopk().get("m1") == 20
          && topkSpec.getTopk().get("m2") == 30, true, "Incorrect topk config");
    topkSpec = topk.get(1);
    Assert.assertEquals(topkSpec.getDimensionName().equals("d3")
        && topkSpec.getTopk().size() == 1
        && topkSpec.getTopk().get("m1") == 50, true, "Incorrect topk config");
    failed = false;
    try {
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_METRICS.toString() + ".d3", "m1");
      props.setProperty(ThirdEyeConfigConstants.THIRDEYE_TOPK_KVALUES.toString() + ".d3", "50,50");
      config = ThirdEyeConfig.fromProperties(props);
    } catch (IllegalStateException e) {
      failed = true;
    }
    Assert.assertTrue(failed, "Expecetd exception due to inequal number of metrics and kvalues for dimension");
    props.remove(ThirdEyeConfigConstants.THIRDEYE_TOPK_DIMENSION_NAMES.toString());
    config = ThirdEyeConfig.fromProperties(props);
    Assert.assertEquals(config.getTopKWhitelist(), null, "Default topk should be null");

  }
}
