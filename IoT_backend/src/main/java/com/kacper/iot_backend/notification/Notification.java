package com.kacper.iot_backend.notification;

import com.kacper.iot_backend.device.Device;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Setter
@Getter
@Entity
@Table(name = "notifications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "notification_type",
            nullable = false
    )
    private String type;

    @Column(
            name = "message",
            nullable = false
    )
    private String message;

    @Column(
            name = "has_seen",
            nullable = false
    )
    private Boolean has_seen;

    @Column(
            name = "timestamp",
            nullable = false
    )
    private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private Device device;

    @Override
    public String toString() {
        return "Notification(id=" + this.getId() + ", type=" + this.getType() + ", message=" + this.getMessage() + ", has_seen=" + this.getHas_seen();
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof Notification)) return false;
        final Notification other = (Notification) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        final Object this$message = this.getMessage();
        final Object other$message = other.getMessage();
        if (this$message == null ? other$message != null : !this$message.equals(other$message)) return false;
        final Object this$has_seen = this.getHas_seen();
        final Object other$has_seen = other.getHas_seen();
        if (this$has_seen == null ? other$has_seen != null : !this$has_seen.equals(other$has_seen)) return false;
        final Object this$device = this.getDevice();
        final Object other$device = other.getDevice();
        if (this$device == null ? other$device != null : !this$device.equals(other$device)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof Notification;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        final Object $message = this.getMessage();
        result = result * PRIME + ($message == null ? 43 : $message.hashCode());
        final Object $has_seen = this.getHas_seen();
        result = result * PRIME + ($has_seen == null ? 43 : $has_seen.hashCode());
        final Object $device = this.getDevice();
        result = result * PRIME + ($device == null ? 43 : $device.hashCode());
        return result;
    }
}
