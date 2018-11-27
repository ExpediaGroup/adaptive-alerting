package com.expedia.adaptivealerting.modelservice.entity;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User info entity. There already exists a class with name User in spring security module. So, naming this entity as UserInfo.
 *
 * @author kashah
 */
@Entity
@Table(name = "user")
@Data
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "username")
    private String userName;

    private String password;

    private String role;

    private boolean enabled;
}