package com.kacper.iot_backend.user;

import com.kacper.iot_backend.activation_token.ActivationToken;
import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.reset_password_token.ResetPasswordToken;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Getter
    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Getter
    @Column(
            name = "last_name",
            nullable = false
    )
    private String last_name;

    @Getter
    @Column(
            name = "email",
            unique = true,
            nullable = false
    )
    private String email;

    @Getter
    @Column(
            name = "password",
            nullable = false
    )
    private String password;

    @Getter
    @Column(
            name = "role",
            nullable = false
    )
    private String role;

    @Getter
    @Column(
            name = "created_at",
            nullable = false
    )
    private Date created_at;

    @Column(
            name = "is_enabled",
            nullable = false
    )
    private boolean enabled;

    @Getter
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            optional = false
    )
    private ActivationToken activationToken;

    @Getter
    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            optional = false
    )
    private ResetPasswordToken resetPasswordToken;

    @Getter
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Device> devices;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", last_name='" + last_name + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", created_at=" + created_at +
                ", enabled=" + enabled +
                '}';
    }


    public static UserBuilder builder() {
        return new UserBuilder();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof User)) return false;
        final User other = (User) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$name = this.getName();
        final Object other$name = other.getName();
        if (this$name == null ? other$name != null : !this$name.equals(other$name)) return false;
        final Object this$last_name = this.getLast_name();
        final Object other$last_name = other.getLast_name();
        if (this$last_name == null ? other$last_name != null : !this$last_name.equals(other$last_name)) return false;
        final Object this$email = this.getEmail();
        final Object other$email = other.getEmail();
        if (this$email == null ? other$email != null : !this$email.equals(other$email)) return false;
        final Object this$password = this.getPassword();
        final Object other$password = other.getPassword();
        if (this$password == null ? other$password != null : !this$password.equals(other$password)) return false;
        final Object this$role = this.getRole();
        final Object other$role = other.getRole();
        if (this$role == null ? other$role != null : !this$role.equals(other$role)) return false;
        final Object this$created_at = this.getCreated_at();
        final Object other$created_at = other.getCreated_at();
        if (this$created_at == null ? other$created_at != null : !this$created_at.equals(other$created_at))
            return false;
        if (this.isEnabled() != other.isEnabled()) return false;
        final Object this$activationToken = this.getActivationToken();
        final Object other$activationToken = other.getActivationToken();
        if (this$activationToken == null ? other$activationToken != null : !this$activationToken.equals(other$activationToken))
            return false;
        final Object this$resetPasswordToken = this.getResetPasswordToken();
        final Object other$resetPasswordToken = other.getResetPasswordToken();
        if (this$resetPasswordToken == null ? other$resetPasswordToken != null : !this$resetPasswordToken.equals(other$resetPasswordToken))
            return false;
        final Object this$devices = this.getDevices();
        final Object other$devices = other.getDevices();
        if (this$devices == null ? other$devices != null : !this$devices.equals(other$devices)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof User;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $name = this.getName();
        result = result * PRIME + ($name == null ? 43 : $name.hashCode());
        final Object $last_name = this.getLast_name();
        result = result * PRIME + ($last_name == null ? 43 : $last_name.hashCode());
        final Object $email = this.getEmail();
        result = result * PRIME + ($email == null ? 43 : $email.hashCode());
        final Object $password = this.getPassword();
        result = result * PRIME + ($password == null ? 43 : $password.hashCode());
        final Object $role = this.getRole();
        result = result * PRIME + ($role == null ? 43 : $role.hashCode());
        final Object $created_at = this.getCreated_at();
        result = result * PRIME + ($created_at == null ? 43 : $created_at.hashCode());
        result = result * PRIME + (this.isEnabled() ? 79 : 97);
        final Object $activationToken = this.getActivationToken();
        result = result * PRIME + ($activationToken == null ? 43 : $activationToken.hashCode());
        final Object $resetPasswordToken = this.getResetPasswordToken();
        result = result * PRIME + ($resetPasswordToken == null ? 43 : $resetPasswordToken.hashCode());
        final Object $devices = this.getDevices();
        result = result * PRIME + ($devices == null ? 43 : $devices.hashCode());
        return result;
    }

    public static class UserBuilder {
        private Integer id;
        private String name;
        private String last_name;
        private String email;
        private String password;
        private String role;
        private Date created_at;
        private boolean enabled;
        private ActivationToken activationToken;
        private ResetPasswordToken resetPasswordToken;
        private List<Device> devices;

        UserBuilder() {
        }

        public UserBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder last_name(String last_name) {
            this.last_name = last_name;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder role(String role) {
            this.role = role;
            return this;
        }

        public UserBuilder created_at(Date created_at) {
            this.created_at = created_at;
            return this;
        }

        public UserBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public UserBuilder activationToken(ActivationToken activationToken) {
            this.activationToken = activationToken;
            return this;
        }

        public UserBuilder resetPasswordToken(ResetPasswordToken resetPasswordToken) {
            this.resetPasswordToken = resetPasswordToken;
            return this;
        }

        public UserBuilder devices(List<Device> devices) {
            this.devices = devices;
            return this;
        }

        public User build() {
            return new User(this.id, this.name, this.last_name, this.email, this.password, this.role, this.created_at, this.enabled, this.activationToken, this.resetPasswordToken, this.devices);
        }

        public String toString() {
            return "User.UserBuilder(id=" + this.id + ", name=" + this.name + ", last_name=" + this.last_name + ", email=" + this.email + ", password=" + this.password + ", role=" + this.role + ", created_at=" + this.created_at + ", enabled=" + this.enabled + ", activationToken=" + this.activationToken + ", resetPasswordToken=" + this.resetPasswordToken + ", devices=" + this.devices + ")";
        }
    }
}
