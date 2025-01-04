package com.kacper.iot_backend.device;

import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class DeviceRepositoryTest {

    @Autowired
    private DeviceRepository deviceRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private Device device1;

    @AfterEach
    public void cleanup() {
        if (device1 != null) {
            deviceRepository.delete(device1);
        }
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Test
    public void shouldSaveDevice() {
        user = new User();
        user.setName("Test");
        user.setLast_name("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(true);

        userRepository.save(user);

        device1 = Device.builder()
                .address("Grove street 1")
                .deviceName("Device 1")
                .uuid("uuid-1")
                .user(user)
                .build();

        device1 = deviceRepository.save(device1);

        assertThat(device1.getDeviceName()).isEqualTo("Device 1");
    }

    @Test
    public void shouldFindDevicesByUser() {
        user = new User();
        user.setName("Test");
        user.setLast_name("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(true);

        userRepository.save(user);

        device1 = Device.builder()
                .address("Grove street 1")
                .deviceName("Device 1")
                .uuid("uuid-1")
                .user(user)
                .build();

        device1 = deviceRepository.save(device1);

        List<Device> foundDevices = deviceRepository.findByUser(user);

        assertThat(foundDevices).hasSize(1);
        assertThat(foundDevices).extracting(Device::getDeviceName).containsOnly(device1.getDeviceName());
    }

    @Test
    public void shouldFindDeviceByUuid() {
        user = new User();
        user.setName("Test");
        user.setLast_name("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(true);

        userRepository.save(user);


        device1 = Device.builder()
                .address("Grove street 1")
                .deviceName("Device 1")
                .uuid("uuid-1")
                .user(user)
                .build();

        device1 = deviceRepository.save(device1);

        Optional<Device> device1 = deviceRepository.findById(this.device1.getId());

        assertThat(device1).isPresent();
        assertThat(device1.get().getUuid()).isEqualTo(this.device1.getUuid());
    }


}
