package com.localtest.s3imdbtitleuploader.data.repository;

import com.localtest.s3imdbtitleuploader.data.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TitleRepository extends JpaRepository<Title, String> {
}
