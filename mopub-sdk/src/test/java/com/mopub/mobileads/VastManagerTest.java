package com.mopub.mobileads;

import android.app.Activity;

import com.mopub.common.CacheService;
import com.mopub.common.test.support.SdkTestRunner;
import com.mopub.common.util.DeviceUtils;
import com.mopub.mobileads.test.support.VastUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.tester.org.apache.http.FakeHttpLayer;

import java.util.concurrent.Semaphore;

import static com.mopub.mobileads.VastManager.VastManagerListener;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(SdkTestRunner.class)
public class VastManagerTest {
    static final String EXTENSIONS_SNIPPET_PLACEHOLDER = "<![CDATA[EXTENSIONS_SNIPPET]]>";
    static final String TEST_VAST_XML_STRING = "<VAST version='2.0'><Ad id='62833'><Wrapper><AdSystem>Tapad</AdSystem><VASTAdTagURI>http://dsp.x-team.staging.mopub.com/xml</VASTAdTagURI><Impression>http://myTrackingURL/wrapper/impression1</Impression><Impression>http://myTrackingURL/wrapper/impression2</Impression><Creatives><Creative AdID='62833'><Linear><TrackingEvents><Tracking event='creativeView'>http://myTrackingURL/wrapper/creativeView</Tracking><Tracking event='start'>http://myTrackingURL/wrapper/start</Tracking><Tracking event='midpoint'>http://myTrackingURL/wrapper/midpoint</Tracking><Tracking event='progress' offset='00:00:03.100'>http://myTrackingURL/wrapper/progress</Tracking><Tracking event='firstQuartile'>http://myTrackingURL/wrapper/firstQuartile</Tracking><Tracking event='thirdQuartile'>http://myTrackingURL/wrapper/thirdQuartile</Tracking><Tracking event='complete'>http://myTrackingURL/wrapper/complete</Tracking><Tracking event='close'>http://myTrackingURL/wrapper/close</Tracking><Tracking event='skip'>http://myTrackingURL/wrapper/skip</Tracking><Tracking event='mute'>http://myTrackingURL/wrapper/mute</Tracking><Tracking event='unmute'>http://myTrackingURL/wrapper/unmute</Tracking><Tracking event='pause'>http://myTrackingURL/wrapper/pause</Tracking><Tracking event='resume'>http://myTrackingURL/wrapper/resume</Tracking><Tracking event='fullscreen'>http://myTrackingURL/wrapper/fullscreen</Tracking></TrackingEvents><VideoClicks><ClickTracking>http://myTrackingURL/wrapper/click</ClickTracking></VideoClicks></Linear></Creative><Creative AdID=\"601364-Companion\"> <CompanionAds><Companion width=\"9000\"></Companion> </CompanionAds></Creative></Creatives><![CDATA[EXTENSIONS_SNIPPET]]><Error><![CDATA[http://wrapperErrorTracker]]></Error></Wrapper></Ad></VAST><MP_TRACKING_URLS><MP_TRACKING_URL>http://www.mopub.com/imp1</MP_TRACKING_URL><MP_TRACKING_URL>http://www.mopub.com/imp2</MP_TRACKING_URL></MP_TRACKING_URLS>";
    static final String TEST_NESTED_VAST_XML_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><VAST version='2.0'><Ad id='57722'><InLine><AdSystem version='1.0'>Tapad</AdSystem><AdTitle><![CDATA[PKW6T_LIV_DSN_Audience_TAPAD_3rd Party Audience Targeting_Action Movi]]></AdTitle><Description/><Impression><![CDATA[http://rtb-test.dev.tapad.com:8080/creative/imp.png?ts=1374099035457&svid=1&creative_id=30731&ctx_type=InApp&ta_pinfo=JnRhX2JpZD1iNDczNTQwMS1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmaXA9OTguMTE2LjEyLjk0JnNzcD1MSVZFUkFJTCZ0YV9iaWRkZXJfaWQ9NTEzJTNBMzA1NSZjdHg9MTMzMSZ0YV9jYW1wYWlnbl9pZD01MTMmZGM9MTAwMjAwMzAyOSZ1YT1Nb3ppbGxhJTJGNS4wKyUyOE1hY2ludG9zaCUzQitJbnRlbCtNYWMrT1MrWCsxMF84XzMlMjkrQXBwbGVXZWJLaXQlMkY1MzcuMzYrJTI4S0hUTUwlMkMrbGlrZStHZWNrbyUyOStDaHJvbWUlMkYyNy4wLjE0NTMuMTE2K1NhZmFyaSUyRjUzNy4zNiZjcHQ9VkFTVCZkaWQ9ZDgyNWZjZDZlNzM0YTQ3ZTE0NWM4ZTkyNzMwMjYwNDY3YjY1NjllMSZpZD1iNDczNTQwMC1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmcGlkPUNPTVBVVEVSJnN2aWQ9MSZicD0zNS4wMCZjdHhfdHlwZT1BJnRpZD0zMDU1JmNyaWQ9MzA3MzE%3D&liverail_cp=1]]></Impression><Creatives><Creative sequence='1' id='57722'><Linear><TrackingEvents><Tracking event='close'>http://myTrackingURL/wrapper/nested_close</Tracking><Tracking event='skip'>http://myTrackingURL/wrapper/nested_skip</Tracking></TrackingEvents><Duration>00:00:15</Duration><VideoClicks><ClickThrough><![CDATA[http://rtb-test.dev.tapad.com:8080/click?ta_pinfo=JnRhX2JpZD1iNDczNTQwMS1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmaXA9OTguMTE2LjEyLjk0JnNzcD1MSVZFUkFJTCZ0YV9iaWRkZXJfaWQ9NTEzJTNBMzA1NSZjdHg9MTMzMSZ0YV9jYW1wYWlnbl9pZD01MTMmZGM9MTAwMjAwMzAyOSZ1YT1Nb3ppbGxhJTJGNS4wKyUyOE1hY2ludG9zaCUzQitJbnRlbCtNYWMrT1MrWCsxMF84XzMlMjkrQXBwbGVXZWJLaXQlMkY1MzcuMzYrJTI4S0hUTUwlMkMrbGlrZStHZWNrbyUyOStDaHJvbWUlMkYyNy4wLjE0NTMuMTE2K1NhZmFyaSUyRjUzNy4zNiZjcHQ9VkFTVCZkaWQ9ZDgyNWZjZDZlNzM0YTQ3ZTE0NWM4ZTkyNzMwMjYwNDY3YjY1NjllMSZpZD1iNDczNTQwMC1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmcGlkPUNPTVBVVEVSJnN2aWQ9MSZicD0zNS4wMCZjdHhfdHlwZT1BJnRpZD0zMDU1JmNyaWQ9MzA3MzE%3D&crid=30731&ta_action_id=click&ts=1374099035458&redirect=http%3A%2F%2Ftapad.com]]></ClickThrough></VideoClicks><MediaFiles><MediaFile delivery='progressive' bitrate='416' width='800' height='480' type='video/mp4'><![CDATA[https://s3.amazonaws.com/mopub-vast/tapad-video.mp4]]></MediaFile><MediaFile delivery='progressive' bitrate='416' width='300' height='250' type='video/mp4'><![CDATA[https://s3.amazonaws.com/mopub-vast/tapad-video1.mp4]]></MediaFile></MediaFiles></Linear></Creative><Creative AdID=\"601364-Companion\"><CompanionAds><Companion id=\"valid\" height=\"250\" width=\"300\"><StaticResource creativeType=\"image/jpeg\">http://demo.tremormedia.com/proddev/vast/Blistex1.jpg</StaticResource><TrackingEvents><Tracking event=\"creativeView\">http://myTrackingURL/firstCompanionCreativeView</Tracking><Tracking event=\"creativeView\">http://myTrackingURL/secondCompanionCreativeView</Tracking></TrackingEvents><CompanionClickThrough>http://www.tremormedia.com</CompanionClickThrough><CompanionClickTracking><![CDATA[http://companionClickTracking1]]></CompanionClickTracking><CompanionClickTracking><![CDATA[http://companionClickTracking2]]></CompanionClickTracking></Companion></CompanionAds></Creative></Creatives><![CDATA[EXTENSIONS_SNIPPET]]><Error><![CDATA[http://inLineErrorTracker]]></Error></InLine></Ad></VAST>";
    static final String TEST_VAST_BAD_NEST_URL_XML_STRING = "<VAST version='2.0'><Ad id='62833'><Wrapper><AdSystem>Tapad</AdSystem><VASTAdTagURI>http://dsp.x-team.staging.mopub.com/xml\"$|||</VASTAdTagURI><Impression>http://myTrackingURL/wrapper/impression1</Impression><Impression>http://myTrackingURL/wrapper/impression2</Impression><Creatives><Creative AdID='62833'><Linear><TrackingEvents><Tracking event='creativeView'>http://myTrackingURL/wrapper/creativeView</Tracking><Tracking event='start'>http://myTrackingURL/wrapper/start</Tracking><Tracking event='midpoint'>http://myTrackingURL/wrapper/midpoint</Tracking><Tracking event='firstQuartile'>http://myTrackingURL/wrapper/firstQuartile</Tracking><Tracking event='thirdQuartile'>http://myTrackingURL/wrapper/thirdQuartile</Tracking><Tracking event='complete'>http://myTrackingURL/wrapper/complete</Tracking><Tracking event='mute'>http://myTrackingURL/wrapper/mute</Tracking><Tracking event='unmute'>http://myTrackingURL/wrapper/unmute</Tracking><Tracking event='pause'>http://myTrackingURL/wrapper/pause</Tracking><Tracking event='resume'>http://myTrackingURL/wrapper/resume</Tracking><Tracking event='fullscreen'>http://myTrackingURL/wrapper/fullscreen</Tracking></TrackingEvents><VideoClicks><ClickTracking>http://myTrackingURL/wrapper/click</ClickTracking></VideoClicks></Linear></Creative></Creatives></Wrapper></Ad></VAST><MP_TRACKING_URLS><MP_TRACKING_URL>http://www.mopub.com/imp1</MP_TRACKING_URL><MP_TRACKING_URL>http://www.mopub.com/imp2</MP_TRACKING_URL></MP_TRACKING_URLS>";

    private VastManager subject;
    private FakeHttpLayer mFakeHttpLayer;
    private VastManagerListener vastManagerListener;
    private Activity context;
    private VastVideoConfiguration vastVideoConfiguration;
    private Semaphore semaphore;

    @Before
    public void setup() {
        context = Robolectric.buildActivity(Activity.class).create().get();
        CacheService.initializeDiskCache(context);
        subject = new VastManager(context);
        mFakeHttpLayer = Robolectric.getFakeHttpLayer();

        semaphore = new Semaphore(0);
        vastManagerListener = mock(VastManagerListener.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                VastManagerTest.this.vastVideoConfiguration = (VastVideoConfiguration) args[0];
                semaphore.release();
                return null;
            }
        }).when(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));
    }

    private void prepareVastVideoConfiguration() {
        subject.prepareVastVideoConfiguration(TEST_VAST_XML_STRING, vastManagerListener, context);

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
    }

    @Test
    public void prepareVastVideoConfiguration_shouldNotifyTheListenerAndContainTheCorrectVastValues() throws Exception {
        // Vast redirect responses
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();
        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        assertThat(vastVideoConfiguration.getNetworkMediaFileUrl()).isEqualTo("https://s3.amazonaws.com/mopub-vast/tapad-video.mp4");

        final String expectedFilePathDiskCache = CacheService.getFilePathDiskCache(vastVideoConfiguration.getNetworkMediaFileUrl());
        assertThat(vastVideoConfiguration.getDiskMediaFileUrl()).isEqualTo(expectedFilePathDiskCache);

        assertThat(vastVideoConfiguration.getClickThroughUrl()).isEqualTo("http://rtb-test.dev.tapad.com:8080/click?ta_pinfo=JnRhX2JpZD1iNDczNTQwMS1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmaXA9OTguMTE2LjEyLjk0JnNzcD1MSVZFUkFJTCZ0YV9iaWRkZXJfaWQ9NTEzJTNBMzA1NSZjdHg9MTMzMSZ0YV9jYW1wYWlnbl9pZD01MTMmZGM9MTAwMjAwMzAyOSZ1YT1Nb3ppbGxhJTJGNS4wKyUyOE1hY2ludG9zaCUzQitJbnRlbCtNYWMrT1MrWCsxMF84XzMlMjkrQXBwbGVXZWJLaXQlMkY1MzcuMzYrJTI4S0hUTUwlMkMrbGlrZStHZWNrbyUyOStDaHJvbWUlMkYyNy4wLjE0NTMuMTE2K1NhZmFyaSUyRjUzNy4zNiZjcHQ9VkFTVCZkaWQ9ZDgyNWZjZDZlNzM0YTQ3ZTE0NWM4ZTkyNzMwMjYwNDY3YjY1NjllMSZpZD1iNDczNTQwMC1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmcGlkPUNPTVBVVEVSJnN2aWQ9MSZicD0zNS4wMCZjdHhfdHlwZT1BJnRpZD0zMDU1JmNyaWQ9MzA3MzE%3D&crid=30731&ta_action_id=click&ts=1374099035458&redirect=http%3A%2F%2Ftapad.com");
        assertThat(vastVideoConfiguration.getImpressionTrackers().size()).isEqualTo(5);

        // Verify quartile trackers
        assertThat(vastVideoConfiguration.getFractionalTrackers().size()).isEqualTo(3);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(0).trackingFraction()).isEqualTo(0.25f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(1).trackingFraction()).isEqualTo(0.5f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(2).trackingFraction()).isEqualTo(0.75f);

        // Verify start tracker.
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().size()).isEqualTo(3);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(0).getTrackingMilliseconds())
                .isEqualTo(0);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(1).getTrackingMilliseconds())
                .isEqualTo(2000);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(2).getTrackingMilliseconds())
                .isEqualTo(3100);

        assertThat(vastVideoConfiguration.getCompleteTrackers().size()).isEqualTo(1);

        // We specifically added a close tracker and a skip tracker to the nested vast test case as well,
        // therefore there are two expected trackers total for each type.
        assertThat(vastVideoConfiguration.getCloseTrackers().size()).isEqualTo(2);
        assertThat(vastVideoConfiguration.getSkipTrackers().size()).isEqualTo(2);
        assertThat(vastVideoConfiguration.getClickTrackers().size()).isEqualTo(1);

        final VastCompanionAd vastCompanionAd = vastVideoConfiguration.getVastCompanionAd();
        assertThat(vastCompanionAd.getWidth()).isEqualTo(300);
        assertThat(vastCompanionAd.getHeight()).isEqualTo(250);
        assertThat(vastCompanionAd.getVastResource().getResource())
                .isEqualTo("http://demo.tremormedia.com/proddev/vast/Blistex1.jpg");
        assertThat(vastCompanionAd.getVastResource().getType())
                .isEqualTo(VastResource.Type.STATIC_RESOURCE);
        assertThat(vastCompanionAd.getVastResource().getCreativeType())
                .isEqualTo(VastResource.CreativeType.IMAGE);
        assertThat(vastCompanionAd.getClickThroughUrl()).isEqualTo("http://www.tremormedia.com");

        assertThat(VastUtils.vastTrackersToStrings(vastCompanionAd.getClickTrackers()))
                .containsOnly("http://companionClickTracking1",
                        "http://companionClickTracking2");
    }

    @Test
    public void prepareVastVideoConfiguration_shouldHandleMultipleRedirects() throws Exception {
        // Vast redirect responses
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_VAST_XML_STRING);
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_VAST_XML_STRING);
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();
        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration
                .class));

        // at this point it should have 3 sets of data from TEST_VAST_XML_STRING and one set from TEST_NESTED_VAST_XML_STRING
        assertThat(vastVideoConfiguration.getNetworkMediaFileUrl()).isEqualTo("https://s3.amazonaws.com/mopub-vast/tapad-video.mp4");
        final String expectedFilePathDiskCache = CacheService.getFilePathDiskCache(vastVideoConfiguration.getNetworkMediaFileUrl());
        assertThat(vastVideoConfiguration.getDiskMediaFileUrl()).isEqualTo(expectedFilePathDiskCache);

        assertThat(vastVideoConfiguration.getClickThroughUrl()).isEqualTo("http://rtb-test.dev.tapad.com:8080/click?ta_pinfo=JnRhX2JpZD1iNDczNTQwMS1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmaXA9OTguMTE2LjEyLjk0JnNzcD1MSVZFUkFJTCZ0YV9iaWRkZXJfaWQ9NTEzJTNBMzA1NSZjdHg9MTMzMSZ0YV9jYW1wYWlnbl9pZD01MTMmZGM9MTAwMjAwMzAyOSZ1YT1Nb3ppbGxhJTJGNS4wKyUyOE1hY2ludG9zaCUzQitJbnRlbCtNYWMrT1MrWCsxMF84XzMlMjkrQXBwbGVXZWJLaXQlMkY1MzcuMzYrJTI4S0hUTUwlMkMrbGlrZStHZWNrbyUyOStDaHJvbWUlMkYyNy4wLjE0NTMuMTE2K1NhZmFyaSUyRjUzNy4zNiZjcHQ9VkFTVCZkaWQ9ZDgyNWZjZDZlNzM0YTQ3ZTE0NWM4ZTkyNzMwMjYwNDY3YjY1NjllMSZpZD1iNDczNTQwMC1lZjJkLTExZTItYTNkNS0yMjAwMGE4YzEwOWQmcGlkPUNPTVBVVEVSJnN2aWQ9MSZicD0zNS4wMCZjdHhfdHlwZT1BJnRpZD0zMDU1JmNyaWQ9MzA3MzE%3D&crid=30731&ta_action_id=click&ts=1374099035458&redirect=http%3A%2F%2Ftapad.com");
        assertThat(vastVideoConfiguration.getImpressionTrackers().size()).isEqualTo(13);

        assertThat(vastVideoConfiguration.getAbsoluteTrackers().size()).isEqualTo(9);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(0).getTrackingMilliseconds()).isEqualTo(0);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(1).getTrackingMilliseconds()).isEqualTo(0);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(2).getTrackingMilliseconds()).isEqualTo(0);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(3).getTrackingMilliseconds()).isEqualTo(2000);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(4).getTrackingMilliseconds()).isEqualTo(2000);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(5).getTrackingMilliseconds()).isEqualTo(2000);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(6).getTrackingMilliseconds()).isEqualTo(3100);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(7).getTrackingMilliseconds()).isEqualTo(3100);
        assertThat(vastVideoConfiguration.getAbsoluteTrackers().get(8).getTrackingMilliseconds()).isEqualTo(3100);


        assertThat(vastVideoConfiguration.getFractionalTrackers().size()).isEqualTo(9);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(0).trackingFraction()).isEqualTo(0.25f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(1).trackingFraction()).isEqualTo(0.25f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(2).trackingFraction()).isEqualTo(0.25f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(3).trackingFraction()).isEqualTo(0.5f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(4).trackingFraction()).isEqualTo(0.5f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(5).trackingFraction()).isEqualTo(0.5f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(6).trackingFraction()).isEqualTo(0.75f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(7).trackingFraction()).isEqualTo(0.75f);
        assertThat(vastVideoConfiguration.getFractionalTrackers().get(8).trackingFraction()).isEqualTo(0.75f);

        assertThat(vastVideoConfiguration.getCompleteTrackers().size()).isEqualTo(3);
        assertThat(vastVideoConfiguration.getCloseTrackers().size()).isEqualTo(4);
        assertThat(vastVideoConfiguration.getSkipTrackers().size()).isEqualTo(4);
        assertThat(vastVideoConfiguration.getClickTrackers().size()).isEqualTo(3);
        assertThat(vastVideoConfiguration.getErrorTrackers().size()).isEqualTo(4);

        final VastCompanionAd vastCompanionAd = vastVideoConfiguration.getVastCompanionAd();
        assertThat(vastCompanionAd.getWidth()).isEqualTo(300);
        assertThat(vastCompanionAd.getHeight()).isEqualTo(250);
        assertThat(vastCompanionAd.getVastResource().getResource())
                .isEqualTo("http://demo.tremormedia.com/proddev/vast/Blistex1.jpg");
        assertThat(vastCompanionAd.getVastResource().getType())
                .isEqualTo(VastResource.Type.STATIC_RESOURCE);
        assertThat(vastCompanionAd.getVastResource().getCreativeType())
                .isEqualTo(VastResource.CreativeType.IMAGE);
        assertThat(vastCompanionAd.getClickThroughUrl()).isEqualTo("http://www.tremormedia.com");
        assertThat(VastUtils.vastTrackersToStrings(vastCompanionAd.getClickTrackers()))
                .containsOnly("http://companionClickTracking1",
                        "http://companionClickTracking2");
    }

    @Test
    public void prepareVastVideoConfiguration_shouldReturnCorrectVastValuesWhenAVastRedirectFails() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(404, "");
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();
        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        assertThat(vastVideoConfiguration).isNull();
    }

    @Test
    public void prepareVastVideoConfiguration_withNoExtensions_shouldContainTheCorrectDefaultExtensionValues() throws Exception {
        // Vast redirect response to XML without VAST extensions
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();
        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        assertThat(vastVideoConfiguration.getCustomCtaText()).isNull();
        assertThat(vastVideoConfiguration.getCustomSkipText()).isNull();
        assertThat(vastVideoConfiguration.getCustomCloseIconUrl()).isNull();
        assertThat(vastVideoConfiguration.getCustomForceOrientation()).isEqualTo(DeviceUtils.ForceOrientation.FORCE_LANDSCAPE);
    }

    @Test
    public void prepareVastVideoConfiguration_withExtensionsUnderWrapper_shouldContainTheCorrectCustomExtensionValues() throws Exception {
        // Vast redirect response to XML without extensions
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        // Add extensions under Wrapper element in TEST_VAST_XML_STRING
        subject.prepareVastVideoConfiguration(
                TEST_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubCtaText>custom CTA text</MoPubCtaText>" +
                                "<MoPubSkipText>skip</MoPubSkipText>" +
                                "<MoPubCloseIcon>http://ton.twitter.com/exchange-media/images/v4/star_icon_3x.png</MoPubCloseIcon>" +
                                "<MoPubForceOrientation>device</MoPubForceOrientation>" +
                            "</Extension>" +
                        "</Extensions>"),
                vastManagerListener,
                context);

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        semaphore.acquire();
        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        // Verify custom extensions
        assertThat(vastVideoConfiguration.getCustomCtaText()).isEqualTo("custom CTA text");
        assertThat(vastVideoConfiguration.getCustomSkipText()).isEqualTo("skip");
        assertThat(vastVideoConfiguration.getCustomCloseIconUrl()).isEqualTo("http://ton.twitter.com/exchange-media/images/v4/star_icon_3x.png");
        assertThat(vastVideoConfiguration.getCustomForceOrientation()).isEqualTo(DeviceUtils.ForceOrientation.DEVICE_ORIENTATION);
    }

    @Test
    public void prepareVastVideoConfiguration_withExtensionsUnderInline_shouldContainTheCorrectCustomExtensionValues() throws Exception {
        // Vast redirect response to XML with extensions under Inline element
        mFakeHttpLayer.addPendingHttpResponse(200,
                TEST_NESTED_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubCtaText>custom CTA text</MoPubCtaText>" +
                                "<MoPubSkipText>skip</MoPubSkipText>" +
                                "<MoPubCloseIcon>http://ton.twitter.com/exchange-media/images/v4/star_icon_3x.png</MoPubCloseIcon>" +
                                "<MoPubForceOrientation>device</MoPubForceOrientation>" +
                            "</Extension>" +
                        "</Extensions>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        // Verify custom extensions
        assertThat(vastVideoConfiguration.getCustomCtaText()).isEqualTo("custom CTA text");
        assertThat(vastVideoConfiguration.getCustomSkipText()).isEqualTo("skip");
        assertThat(vastVideoConfiguration.getCustomCloseIconUrl()).isEqualTo("http://ton.twitter.com/exchange-media/images/v4/star_icon_3x.png");
        assertThat(vastVideoConfiguration.getCustomForceOrientation()).isEqualTo(DeviceUtils.ForceOrientation.DEVICE_ORIENTATION);
    }

    @Test
    public void prepareVastVideoConfiguration_withExtensionsUnderBothWrapperAndInline_shouldContainLastParsedCustomExtensionValues() throws Exception {
        // Vast redirect response to XML with extensions under Inline element in TEST_NESTED_VAST_XML_STRING, will be parsed last
        mFakeHttpLayer.addPendingHttpResponse(200,
                TEST_NESTED_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubCtaText>CTA 2</MoPubCtaText>" +
                                "<MoPubSkipText>skip 2</MoPubSkipText>" +
                                "<MoPubCloseIcon>http://ton.twitter.com/exchange-media/images/v4/star_icon_3x_2.png</MoPubCloseIcon>" +
                                "<MoPubForceOrientation>landscape</MoPubForceOrientation>" +
                            "</Extension>" +
                        "</Extensions>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        // Also add extensions under Wrapper element in TEST_VAST_XML_STRING
        subject.prepareVastVideoConfiguration(
                TEST_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubCtaText>CTA 1</MoPubCtaText>" +
                                "<MoPubSkipText>skip 1</MoPubSkipText>" +
                                "<MoPubCloseIcon>http://ton.twitter.com/exchange-media/images/v4/star_icon_3x_1.png</MoPubCloseIcon>" +
                                "<MoPubForceOrientation>device orientation</MoPubForceOrientation>" +
                            "</Extension>" +
                        "</Extensions>"),
                vastManagerListener,
                context);

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        // Verify custom extension values are the ones last parsed in TEST_NESTED_VAST_XML_STRING
        assertThat(vastVideoConfiguration.getCustomCtaText()).isEqualTo("CTA 2");
        assertThat(vastVideoConfiguration.getCustomSkipText()).isEqualTo("skip 2");
        assertThat(vastVideoConfiguration.getCustomCloseIconUrl()).isEqualTo("http://ton.twitter.com/exchange-media/images/v4/star_icon_3x_2.png");
        assertThat(vastVideoConfiguration.getCustomForceOrientation()).isEqualTo(DeviceUtils.ForceOrientation.FORCE_LANDSCAPE);
    }

    @Test
    public void prepareVastVideoConfiguration_withCustomCtaTextAsSingleSpace_shouldReturnEmptyString() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200,
                TEST_NESTED_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubCtaText> </MoPubCtaText>" +     // single space, i.e. no text
                            "</Extension>" +
                        "</Extensions>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));
        assertThat(vastVideoConfiguration.getCustomCtaText()).isEmpty();
    }

    @Test
    public void prepareVastVideoConfiguration_withCustomCtaTextLongerThan15Chars_shouldReturnNull() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200,
                TEST_NESTED_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubCtaText>1234567890123456</MoPubCtaText>" +     // 16 chars
                            "</Extension>" +
                        "</Extensions>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));
        assertThat(vastVideoConfiguration.getCustomCtaText()).isNull();
    }

    @Test
    public void prepareVastVideoConfiguration_withCustomSkipTextLongerThan8Chars_shouldReturnNull() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200,
                TEST_NESTED_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubSkipText>123456789</MoPubSkipText>" +     // 9 chars
                            "</Extension>" +
                        "</Extensions>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));
        assertThat(vastVideoConfiguration.getCustomSkipText()).isNull();
    }

    @Test
    public void prepareVastVideoConfiguration_withInvalidCustomForceOrientation_shouldReturnDefaultForceLandscapeOrientation() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200,
                TEST_NESTED_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubForceOrientation>abcd</MoPubForceOrientation>" +   // invalid value
                            "</Extension>" +
                        "</Extensions>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));
        assertThat(vastVideoConfiguration.getCustomForceOrientation()).isEqualTo(DeviceUtils.ForceOrientation.FORCE_LANDSCAPE);
    }

    @Test
    public void prepareVastVideoConfiguration_withCustomForceOrientationInMixedCaseAndUntrimmed_shouldReturnCustomForceOrientation() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200,
                TEST_NESTED_VAST_XML_STRING.replace(EXTENSIONS_SNIPPET_PLACEHOLDER,
                        "<Extensions>" +
                            "<Extension type=\"MoPub\">" +
                                "<MoPubForceOrientation> PortRAIT  </MoPubForceOrientation>" +
                            "</Extension>" +
                        "</Extensions>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));
        assertThat(vastVideoConfiguration.getCustomForceOrientation()).isEqualTo(DeviceUtils.ForceOrientation.FORCE_PORTRAIT);
    }

    @Test
    public void prepareVastVideoConfiguration_withValidPercentSkipOffset_shouldReturnCorrectValue() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING.replace("<Linear>", "<Linear skipoffset='25%'>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        assertThat(vastVideoConfiguration.getSkipOffset()).isEqualTo("25%");
    }


    @Test
    public void prepareVastVideoConfiguration_withValidAbsoluteSkipOffset_shouldReturnCorrectValue() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING.replace("<Linear>", "<Linear skipoffset='  00:03:14 '>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        assertThat(vastVideoConfiguration.getSkipOffset()).isEqualTo("00:03:14");
    }

    @Test
    public void prepareVastVideoConfiguration_withValidAbsoluteSkipOffsetWithExtraSpace_shouldReturnCorrectTrimmedValue() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING.replace("<Linear>", "<Linear skipoffset='  00:03:14.159 '>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        assertThat(vastVideoConfiguration.getSkipOffset()).isEqualTo("00:03:14.159");
    }

    @Test
    public void prepareVastVideoConfiguration_withSkipOffsets_shouldReturnLastParsedValue() throws Exception {
        // Vast redirect response with skipoffset in percent format
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING.replace("<Linear>", "<Linear skipoffset='25%'>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        // Also add a skipoffset in absolute format
        subject.prepareVastVideoConfiguration(
                TEST_VAST_XML_STRING.replace("<Linear>", "<Linear skipoffset='00:03:14'>"),
                vastManagerListener,
                context);

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        // Verify that the last parsed skipoffset value is returned
        assertThat(vastVideoConfiguration.getSkipOffset()).isEqualTo("25%");
    }

    @Test
    public void prepareVastVideoConfiguration_withEmptySkipOffset_shouldReturnNull() throws Exception {
        // Vast redirect response
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING.replace("<Linear>", "<Linear skipoffset=' '>"));
        // Video download response
        mFakeHttpLayer.addPendingHttpResponse(200, "video_data");

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));

        assertThat(vastVideoConfiguration.getSkipOffset()).isNull();
    }

    @Test
    public void prepareVastVideoConfiguration_withNoMediaUrlInXml_shouldReturnNull() throws Exception {
        subject.prepareVastVideoConfiguration(TEST_VAST_BAD_NEST_URL_XML_STRING,
                vastManagerListener, context);

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(null);
        assertThat(vastVideoConfiguration).isEqualTo(null);
    }

    @Test
    public void prepareVastVideoConfiguration_withNullXml_shouldReturnNull() throws Exception {
        subject.prepareVastVideoConfiguration(null, vastManagerListener, context);

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(null);
        assertThat(vastVideoConfiguration).isEqualTo(null);
    }

    @Test
    public void prepareVastVideoConfiguration_withEmptyXml_shouldReturnNull() throws Exception {
        subject.prepareVastVideoConfiguration("", vastManagerListener, context);

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(null);
        assertThat(vastVideoConfiguration).isEqualTo(null);
    }

    @Test
    public void prepareVastVideoConfiguration_withVideoInDiskCache_shouldNotDownloadVideo() throws Exception {
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);

        CacheService.putToDiskCache("https://s3.amazonaws.com/mopub-vast/tapad-video.mp4", "video_data".getBytes());

        prepareVastVideoConfiguration();
        semaphore.acquire();

        assertThat(mFakeHttpLayer.getSentHttpRequestInfos().size()).isEqualTo(1);
        verify(vastManagerListener).onVastVideoConfigurationPrepared(any(VastVideoConfiguration.class));
        assertThat(vastVideoConfiguration.getDiskMediaFileUrl())
                .isEqualTo(CacheService.getFilePathDiskCache("https://s3.amazonaws.com/mopub-vast/tapad-video.mp4"));
    }

    @Test
    public void prepareVastVideoConfiguration_withUninitializedDiskCache_shouldReturnNull() throws Exception {
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);

        prepareVastVideoConfiguration();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(null);
        assertThat(vastVideoConfiguration).isEqualTo(null);
    }

    @Test
    public void cancel_shouldCancelBackgroundProcessingAndNotNotifyListenerWithNull() throws Exception {
        mFakeHttpLayer.addPendingHttpResponse(200, TEST_NESTED_VAST_XML_STRING);

        Robolectric.getBackgroundScheduler().pause();

        subject.prepareVastVideoConfiguration(TEST_VAST_XML_STRING, vastManagerListener, context);

        subject.cancel();

        Robolectric.runBackgroundTasks();
        Robolectric.runUiThreadTasks();
        semaphore.acquire();

        verify(vastManagerListener).onVastVideoConfigurationPrepared(null);
        assertThat(vastVideoConfiguration).isEqualTo(null);
    }
}
