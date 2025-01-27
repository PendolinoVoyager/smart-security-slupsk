package com.kacper.iot_backend.device;

import com.kacper.iot_backend.notification.Notification;
import com.kacper.iot_backend.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "devices")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "address",
            nullable = false
    )
    private String address;

    @Column(
            name = "device_name",
            nullable = false
    )
    private String deviceName;

    @Column(
            name = "uuid",
            nullable = false
    )
    private String uuid;

    @ManyToOne
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Notification> notifications;

    @Override
    public String toString() {
        return "Device(id=" + this.getId() + ", address=" + this.getAddress() + ", deviceName=" + this.getDeviceName() + ", uuid=" + this.getUuid();
    }

    public Integer getId() {
        return this.id;
    }

    public String getAddress() {
        return this.address;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public String getUuid() {
        return this.uuid;
    }

    public User getUser() {
        return this.user;
    }

    public List<Notification> getNotifications() {
        return this.notifications;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Device)) return false;
        final Device other = (Device) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$address = this.getAddress();
        final Object other$address = other.getAddress();
        if (this$address == null ? other$address != null : !this$address.equals(other$address)) return false;
        final Object this$deviceName = this.getDeviceName();
        final Object other$deviceName = other.getDeviceName();
        if (this$deviceName == null ? other$deviceName != null : !this$deviceName.equals(other$deviceName))
            return false;
        final Object this$uuid = this.getUuid();
        final Object other$uuid = other.getUuid();
        if (this$uuid == null ? other$uuid != null : !this$uuid.equals(other$uuid)) return false;
        final Object this$user = this.getUser();
        final Object other$user = other.getUser();
        if (this$user == null ? other$user != null : !this$user.equals(other$user)) return false;
        final Object this$notifications = this.getNotifications();
        final Object other$notifications = other.getNotifications();
        if (this$notifications == null ? other$notifications != null : !this$notifications.equals(other$notifications))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Device;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $address = this.getAddress();
        result = result * PRIME + ($address == null ? 43 : $address.hashCode());
        final Object $deviceName = this.getDeviceName();
        result = result * PRIME + ($deviceName == null ? 43 : $deviceName.hashCode());
        final Object $uuid = this.getUuid();
        result = result * PRIME + ($uuid == null ? 43 : $uuid.hashCode());
        final Object $user = this.getUser();
        result = result * PRIME + ($user == null ? 43 : $user.hashCode());
        final Object $notifications = this.getNotifications();
        result = result * PRIME + ($notifications == null ? 43 : $notifications.hashCode());
        return result;
    }

}
