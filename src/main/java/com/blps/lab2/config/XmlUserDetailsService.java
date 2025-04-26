package com.blps.lab2.config;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class XmlUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            InputStream inputStream = XmlUserDetailsService.class.getClassLoader().getResourceAsStream("users.xml");
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputStream);


            NodeList userList = doc.getElementsByTagName("user");
            for (int i = 0; i < userList.getLength(); i++) {
                Element userElement = (Element) userList.item(i);
                String xmlUsername = userElement.getElementsByTagName("username").item(0).getTextContent();
                if (xmlUsername.equals(username)) {
                    String password = userElement.getElementsByTagName("password").item(0).getTextContent();
                    String authorities = userElement.getElementsByTagName("authorities").item(0).getTextContent();
                    String roles = userElement.getElementsByTagName("roles").item(0).getTextContent();

                    List<String> authoritiesFull = new ArrayList<>(Arrays.asList(authorities.split(",")));
                    authoritiesFull.addAll(
                            Arrays.stream(roles.split(","))
                                    .map(el -> "ROLE_" + el)
                                    .toList()
                    );

                    UserDetails user =  User.builder()
                            .username(xmlUsername)
                            .password(password)
                            .authorities(
                                    authoritiesFull
                                            .stream()
                                            .map(SimpleGrantedAuthority::new)
                                            .toList()
                            )
                            .build();

                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
