package com.codecozy.server.service;

import com.codecozy.server.dto.CustomUserDetails;
import com.codecozy.server.entity.Member;
import com.codecozy.server.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Long memberId = Long.valueOf(username);
        Member member = memberRepository.findByMemberId(memberId);

        if (member == null) {
            throw new UsernameNotFoundException("(토큰 오류) 해당 유저가 존재하지 않습니다.");
        }

        return new CustomUserDetails(username);
    }
}
