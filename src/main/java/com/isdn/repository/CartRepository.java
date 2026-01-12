package com.isdn.repository;

import com.isdn.model.Cart;
import com.isdn.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    Optional<Cart> findByUser_UserId(Long userId);

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.userId = :userId")
    Optional<Cart> findByUserIdWithItems(@Param("userId") Long userId);

    void deleteByUser(User user);
}