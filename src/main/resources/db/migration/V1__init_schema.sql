

CREATE TABLE `users` (
   `user_id` int NOT NULL AUTO_INCREMENT,
   `email` varchar(255) DEFAULT NULL,
   `password` varchar(255) DEFAULT NULL,
   `role` varchar(20) NOT NULL,
   `username` varchar(255) DEFAULT NULL,
   `created_at` date DEFAULT NULL,
   `identity_no` varchar(255) DEFAULT NULL,
   `institution_id` int NOT NULL,
   PRIMARY KEY (`user_id`),
   UNIQUE KEY `UK_user_identity_no` (`identity_no`),
   KEY `FKes3l5tviwmnu2d0gy350kdfht` (`institution_id`),
   CONSTRAINT `FKes3l5tviwmnu2d0gy350kdfht` FOREIGN KEY (`institution_id`) REFERENCES `institution` (`institution_id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

 CREATE TABLE `institution` (
    `institution_id` int NOT NULL AUTO_INCREMENT,
    `address` varchar(255) DEFAULT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `email` varchar(255) NOT NULL,
    `institution_code` varchar(255) NOT NULL,
    `institution_name` varchar(255) NOT NULL,
    `created_by` int DEFAULT NULL,
    PRIMARY KEY (`institution_id`),
    UNIQUE KEY `UKnq8dt2nmo0cfnaqntmi7cwnrt` (`institution_code`),
    UNIQUE KEY `UK9grdn54hea5ns8ahq4yogpb7u` (`email`),
    KEY `FK91jyv029cqw3xx9uq6rc20ice` (`created_by`),
    CONSTRAINT `FK91jyv029cqw3xx9uq6rc20ice` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `instructor` (
   `instructor_id` int NOT NULL AUTO_INCREMENT,
   `avg_rating` double NOT NULL,
   `feedback_count` bigint NOT NULL,
   `is_deleted` bit(1) DEFAULT NULL,
   `deleted_by` varchar(255) DEFAULT NULL,
   `restored_by` varchar(255) DEFAULT NULL,
   `deleted_at` datetime(6) DEFAULT NULL,
   `user_id` int NOT NULL,
   PRIMARY KEY (`instructor_id`),
   UNIQUE KEY `UKcr0g7gh88hv7sfdx9kqbrbiyw` (`user_id`),
   CONSTRAINT `FKl05wgmungp55i9sr39da79agy` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ;


CREATE TABLE `course` (
   `course_id` int NOT NULL AUTO_INCREMENT,
   `course_name` varchar(255) DEFAULT NULL,
   `course_description` varchar(255) DEFAULT NULL,
   `instructor_id` int DEFAULT NULL,
   `created_by` varchar(255) DEFAULT NULL,
   `modified_by` varchar(255) DEFAULT NULL,
   `is_deleted` bit(1) NOT NULL,
   `deleted_by` varchar(255) DEFAULT NULL,
   `restored_by` varchar(255) DEFAULT NULL,
   `avg_rating` double DEFAULT NULL,
   `feedback_count` bigint DEFAULT NULL,
   `deleted_at` datetime(6) DEFAULT NULL,
   `institution_id` int NOT NULL,
   PRIMARY KEY (`course_id`),
   KEY `FK_course_instructor` (`instructor_id`),
   KEY `FKeia6xjuespxibcj584q9k69b1` (`institution_id`),
   CONSTRAINT `FK_course_instructor` FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`instructor_id`),
   CONSTRAINT `FKeia6xjuespxibcj584q9k69b1` FOREIGN KEY (`institution_id`) REFERENCES `institution` (`institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `enrollment` (
   `enroll_id` int NOT NULL AUTO_INCREMENT,
   `enrollment_date` date DEFAULT NULL,
   `course_id` int NOT NULL,
   `institution_id` int NOT NULL,
   `student_id` int NOT NULL,
   PRIMARY KEY (`enroll_id`),
   KEY `FKbhhcqkw1px6yljqg92m0sh2gt` (`course_id`),
   KEY `FKstj1k6nv0jp204gfu6yfdk1pj` (`institution_id`),
   KEY `FKl16dtl7cgm3p2kfip5pml5jsh` (`student_id`),
   CONSTRAINT `FKbhhcqkw1px6yljqg92m0sh2gt` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
   CONSTRAINT `FKl16dtl7cgm3p2kfip5pml5jsh` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`),
   CONSTRAINT `FKstj1k6nv0jp204gfu6yfdk1pj` FOREIGN KEY (`institution_id`) REFERENCES `institution` (`institution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `feedback` (
   `feedback_id` int NOT NULL AUTO_INCREMENT,
   `anonymous` bit(1) NOT NULL,
   `course_comment` varchar(1000) DEFAULT NULL,
   `course_rating` int NOT NULL,
   `instructor_comment` varchar(1000) DEFAULT NULL,
   `instructor_rating` int NOT NULL,
   `submitted_at` datetime(6) NOT NULL,
   `course_id` int DEFAULT NULL,
   `student_id` int DEFAULT NULL,
   `instructor_id` int NOT NULL,
   `is_deleted` tinyint(1) NOT NULL DEFAULT '0',
   `deleted_at` datetime(6) DEFAULT NULL,
   `deleted_by` varchar(255) DEFAULT NULL,
   `restored_by` varchar(255) DEFAULT NULL,
   `institution_id` int NOT NULL,
   PRIMARY KEY (`feedback_id`),
   KEY `FK_feedback_student` (`student_id`),
   KEY `idx_feedback_course` (`course_id`),
   KEY `idx_feedback_instructor` (`instructor_id`),
   KEY `idx_feedback_submitted_at` (`submitted_at`),
   KEY `idx_feedback_institution` (`institution_id`),
   CONSTRAINT `FK58uq9ifknd1ug7u6rxa86jfob` FOREIGN KEY (`institution_id`) REFERENCES `institution` (`institution_id`),
   CONSTRAINT `FK_feedback_course` FOREIGN KEY (`course_id`) REFERENCES `course` (`course_id`),
   CONSTRAINT `FK_feedback_instructor` FOREIGN KEY (`instructor_id`) REFERENCES `instructor` (`instructor_id`),
   CONSTRAINT `FK_feedback_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `refresh_tokens` (
   `id` int NOT NULL AUTO_INCREMENT,
   `token` varchar(300) NOT NULL,
   `created_at` datetime(6) NOT NULL,
   `expires_at` datetime(6) NOT NULL,
   `replaced_at` datetime(6) DEFAULT NULL,
   `revoked` bit(1) NOT NULL,
   `user_id` int NOT NULL,
   PRIMARY KEY (`id`),
   UNIQUE KEY `UK_refresh_token` (`token`),
   UNIQUE KEY `UKghpmfn23vmxfu3spu3lfg4r2d` (`token`),
   KEY `IDX7tdcd6ab5wsgoudnvj7xf1b7l` (`user_id`),
   CONSTRAINT `FK_refresh_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;