package com.udacity.security.service;

import com.udacity.image.service.ImageService;
import com.udacity.security.application.StatusListener;
import com.udacity.security.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    private SecurityService securityService;
    @Mock
    private SecurityRepository securityRepository;
    @Mock
    private ImageService imageService;

    @Mock
    private StatusListener statusListener;

    private Set<Sensor> createSensors(int countSensor, SensorType sensorType, boolean status) {
        Set<Sensor> sensors = new HashSet<>();
        for (int i = 0; i < countSensor; i++) {
            sensors.add(new Sensor(String.valueOf(i), sensorType));
        }
        sensors.forEach(sensor -> sensor.setActive(status));

        return sensors;
    }

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    @DisplayName("1. Alarm is armed and a sensor becomes activated, put the system into pending alarm status.")
    public void alarmArmedAndSensorActivated_changeStatusToPending() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Sensor sensor = new Sensor("Window", SensorType.WINDOW);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    @DisplayName("2. Alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.")
    public void alarmArmedAndSensorActivatedAndSystemPendingAlarm_changeStatusToAlarm() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    @DisplayName("3. Pending alarm and all sensors are inactive, return to no alarm state")
    public void pendingAlarmAndSensorInactive_changeStatusToNoAlarm() {
        Sensor sensor = new Sensor("MOTION", SensorType.MOTION);
        sensor.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @DisplayName("4. Alarm is active, change in sensor state should not affect the alarm state.")
    @ValueSource(booleans = {true, false})
    public void alarmActive_changeSensorStateShouldNotAffectAlarmState(boolean status) {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        Sensor sensor = new Sensor("Window_4", SensorType.WINDOW);
        securityService.changeSensorActivationStatus(sensor, status);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    @DisplayName("5. A sensor is activated while already active and the system is in pending state, change it to alarm state.")
    public void aSensorActivatedWhenAlreadyActiveAndSystemPendingAlarm_changeToAlarmState() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        Sensor sensor = new Sensor("Motion", SensorType.MOTION);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @DisplayName("6. A sensor is deactivated while already inactive, make no changes to the alarm state.")
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM", "ALARM"})
    public void sensorDeactivatedWhileAlreadyInactive_noChangeToAlarmState(AlarmStatus alarmStatus) {
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        Sensor sensor = new Sensor("Door", SensorType.DOOR);
        sensor.setActive(false);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    @DisplayName("7. the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.")
    public void imageContainACatWhileSystemArmedHome_changeStatusToAlarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB));
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @DisplayName("8. the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.")
    @ValueSource(ints = {1, 2, 3, 4})
    public void imageNoContainACat_changeStatusToNoAlarm(int countSensor) {
        Set<Sensor> sensors = createSensors(countSensor, SensorType.DOOR, false);
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB));
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    @DisplayName("9. the system is disarmed, set the status to no alarm.")
    public void systemDisarmed_changeStatusToNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @DisplayName("10. the system is armed, reset all sensors to inactive.")
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    public void systemArmed_resetAllSensorToInactive(ArmingStatus armingStatus) {
        Set<Sensor> sensors = createSensors(4, SensorType.MOTION, true);
        when(securityRepository.getSensors()).thenReturn(sensors);
        securityService.setArmingStatus(armingStatus);
        securityService.getSensors().forEach(
                sensor -> Assertions.assertFalse(sensor.getActive())
        );
    }

    @ParameterizedTest
    @DisplayName("11.  the system is armed-home while the camera shows a cat, set the alarm status to alarm.")
    @EnumSource(value = ArmingStatus.class, names = {"DISARMED", "ARMED_HOME", "ARMED_AWAY"})
    public void systemArmedHomeWhileCameraShowACat_setStatusToAlarm(ArmingStatus armingStatus) {
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(new BufferedImage(100, 200, BufferedImage.TYPE_INT_RGB));
        securityService.setArmingStatus(armingStatus);

        if (armingStatus == ArmingStatus.ARMED_HOME) {
            verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    @Test
    @DisplayName("Add new a sensor")
    public void addNewASensor() {
        Sensor sensor = new Sensor("New sensor", SensorType.DOOR);
        securityService.addSensor(sensor);
        verify(securityRepository, times(1)).addSensor(sensor);
    }

    @Test
    @DisplayName("Remove new a sensor")
    public void removeASensor() {
        Sensor sensor = new Sensor("New sensor", SensorType.DOOR);
        securityService.removeSensor(sensor);
        verify(securityRepository, times(1)).removeSensor(sensor);
    }

    @Test
    @DisplayName("Add Status Listener")
    public void addStatusListener() {
        securityService.addStatusListener(statusListener);
    }

    @Test
    @DisplayName("Add Status Listener")
    public void removeStatusListener() {
        securityService.removeStatusListener(statusListener);
    }
}
