package com.localtest.s3imdbtitleuploader.data.entity;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "title")
@ToString
@SequenceGenerator(name = "title_id_seq", sequenceName = "title_id_seq", allocationSize = 1)
public class Title {

    @Id
    @GeneratedValue(generator = "title_id_seq", strategy = GenerationType.SEQUENCE)
    private String id;

    @Column(name = "title_type")
    private String titleType;

    @Column(name = "primary_title")
    private String primaryTitle;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(name = "is_adult")
    private Boolean isAdult;

    @Column(name = "start_year")
    private Integer startYear;

    @Column(name = "end_year")
    private Integer endYear;

    @Column(name = "runtime_minutes")
    private Integer runtimeMinutes;

    @Column(name = "genres")
    private String genres;
}
