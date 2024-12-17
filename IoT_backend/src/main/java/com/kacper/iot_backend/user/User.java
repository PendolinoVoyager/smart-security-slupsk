package com.kacper.iot_backend.user;

import com.kacper.iot_backend.activation_token.ActivationToken;
import com.kacper.iot_backend.device.Device;
import com.kacper.iot_backend.reset_password_token.ResetPasswordToken;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Builder
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(
            name = "name",
            nullable = false
    )
    private String name;

    @Column(
            name = "last_name",
            nullable = false
    )
    private String last_name;

    @Column(
            name = "email",
            unique = true,
            nullable = false
    )
    private String email;

    @Column(
            name = "password",
            nullable = false
    )
    private String password;

    @Column(
            name = "role",
            nullable = false
    )
    private String role;

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

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            optional = false
    )
    private ActivationToken activationToken;

    @OneToOne(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            optional = false
    )
    private ResetPasswordToken resetPasswordToken;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Device> devices;

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
}
