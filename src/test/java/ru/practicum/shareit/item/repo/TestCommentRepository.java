package ru.practicum.shareit.item.repo;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@DataJpaTest
class TestCommentRepository {
    @Autowired
    private TestEntityManager em;
    @Autowired
    private CommentRepository commentRepository;

    @Test
    void findCommentsByItem_Id() {
        User user = User.builder()
                .name("name")
                .email("user@gmail.com")
                .build();
        Item first = Item.builder()
                .name("first")
                .owner(user)
                .available(true)
                .description("good")
                .build();
        Comment comment = Comment.builder()
                .item(first)
                .text("very good")
                .author(user)
                .created(LocalDateTime.now())
                .build();

        em.persist(user);
        em.persist(first);
        em.persist(comment);

        List<Comment> comments = commentRepository.findCommentsByItem_Id(first.getId());

        Assertions.assertEquals(1, comments.size());
        assertThat(comments.get(0), equalTo(comment));
    }
}