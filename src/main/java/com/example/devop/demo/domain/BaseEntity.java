package com.example.devop.demo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SoftDelete;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SoftDelete(columnName = "is_deleted")
public abstract class BaseEntity {

    @Id
//    @GeneratedValue(generator = "snowflake")
//    @GenericGenerator(
//            name = "snowflake",
//            type = com.example.devop.demo.infrastructure.idGen.SnowflakeIdGeneratorImpl.class
//    )
    protected Long id;

    @CreatedBy
    @Column(updatable = false)
    protected Long createdBy;

    @CreatedDate
    @Column(updatable = false)
    protected LocalDateTime createdDate;

    @LastModifiedBy
    protected Long modifiedBy;

    @LastModifiedDate
    protected LocalDateTime modifiedDate;

//    @Version
//    protected Long originalVersion;
}
