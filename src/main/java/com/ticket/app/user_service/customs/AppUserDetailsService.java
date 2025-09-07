package com.ticket.app.user_service.customs;

import com.ticket.app.user_service.model.UserInfo;
import com.ticket.app.user_service.repository.UserInfoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AppUserDetailsService implements UserDetailsService {

    private final UserInfoRepository userInfoRepository;

    public AppUserDetailsService(UserInfoRepository userInfoRepository) {
        this.userInfoRepository = userInfoRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserInfo user = userInfoRepository.findByEmail(email).orElseThrow(() ->
                new UsernameNotFoundException("User Not Found"));

        return new AppUserDetails(user);
    }
}
