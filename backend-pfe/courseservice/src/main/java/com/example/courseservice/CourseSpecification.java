package com.example.courseservice;

import com.example.courseservice.model.Course;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.Predicate;


public class CourseSpecification {
    public static Specification<Course> filterCourses(String keyword, Long categoryId, Long instructorId) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isEmpty()) {
                Predicate titlePredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + keyword.toLowerCase() + "%");
                Predicate descPredicate = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + keyword.toLowerCase() + "%");
                predicates.add(criteriaBuilder.or(titlePredicate, descPredicate));
            }

            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }

            if (instructorId != null) {
                predicates.add(criteriaBuilder.equal(root.get("instructorId"), instructorId));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

