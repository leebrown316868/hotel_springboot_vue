package com.hotel.specification;

import com.hotel.entity.Guest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Predicate;

public class GuestSpecification {

    public static Specification<Guest> withSearchQuery(String searchQuery) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtils.isBlank(searchQuery)) {
                return criteriaBuilder.conjunction();
            }

            // 按姓名或邮箱模糊匹配（忽略大小写）
            Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + searchQuery.toLowerCase() + "%"
            );

            Predicate emailPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("email")),
                    "%" + searchQuery.toLowerCase() + "%"
            );

            return criteriaBuilder.or(namePredicate, emailPredicate);
        };
    }
}
