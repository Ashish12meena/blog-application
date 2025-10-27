package com.bloggera.blog.config;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bloggera.blog.model.User;
import com.bloggera.blog.model.UserDetailImpl;
import com.bloggera.blog.service.impl.UserServiceImpl;

import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
public class MyUserDetailService implements UserDetailsService {
    private final UserServiceImpl uServiceImpl;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        User user = uServiceImpl.getUserByUsername(username);
        if (user==null) {
            
            throw new UsernameNotFoundException("User Not Found");
        }

        return new UserDetailImpl(user);
    }
    
}