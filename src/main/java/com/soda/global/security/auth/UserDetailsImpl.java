package com.soda.global.security.auth;

import com.soda.member.entity.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserDetailsImpl implements UserDetails {

    // User 엔티티 객체를 담는 필드
    private final Member member;

    public UserDetailsImpl(Member member) {
        // 생성자를 통해 User 엔티티 객체를 초기화
        this.member = member;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자의 권한 목록을 반환하는 메서드
        // 여기서는 User 엔티티의 role 필드를 사용하여 SimpleGrantedAuthority 객체를 생성하고,
        // Collections.singletonList()를 사용하여 권한 목록을 생성하여 반환합니다.
        return Collections.singletonList(new SimpleGrantedAuthority(member.getRole().getDescription()));
    }

    @Override
    public String getPassword() {
        // 사용자의 비밀번호를 반환하는 메서드
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        // 사용자의 아이디를 반환하는 메서드
        return member.getAuthId();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부를 반환하는 메서드
        // 여기서는 항상 true를 반환하여 계정이 만료되지 않았음을 나타냅니다.
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부를 반환하는 메서드
        // 여기서는 항상 true를 반환하여 계정이 잠금되지 않았음을 나타냅니다.
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 비밀번호 만료 여부를 반환하는 메서드
        // 여기서는 항상 true를 반환하여 비밀번호가 만료되지 않았음을 나타냅니다.
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부를 반환하는 메서드
        // 여기서는 항상 true를 반환하여 계정이 활성화되었음을 나타냅니다.
        return true;
    }
}
