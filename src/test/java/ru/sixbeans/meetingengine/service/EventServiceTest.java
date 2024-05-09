package ru.sixbeans.meetingengine.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.sixbeans.meetingengine.entity.Event;
import ru.sixbeans.meetingengine.entity.PersonalInfo;
import ru.sixbeans.meetingengine.entity.Tag;
import ru.sixbeans.meetingengine.entity.User;
import ru.sixbeans.meetingengine.mapper.EventMapper;
import ru.sixbeans.meetingengine.mapper.EventMapperImpl;
import ru.sixbeans.meetingengine.model.EventData;
import ru.sixbeans.meetingengine.service.event.impl.EventService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({EventService.class, EventMapperImpl.class})
public class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private User anotherUser;
    private Event event;
    private Event newEvent;
    private Event nonActiveEvent;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .subject("externalId1")
                .provider("provider1")
                .personalInfo(PersonalInfo.builder()
                        .fullName("Full Name").build())
                .userName("username")
                .email("email@example.com")
                .profileDescription("Profile Description")
                .build();

        anotherUser = User.builder()
                .subject("externalId2")
                .provider("provider2")
                .personalInfo(PersonalInfo.builder()
                        .fullName("Another Full Name")
                        .build())
                .userName("anotherUsername")
                .email("anotherEmail@example.com")
                .profileDescription("Another Profile Description")
                .build();

        Tag tag = Tag.builder()
                .title("hello")
                .category("cat")
                .build();

        event = Event.builder()
                .title("Event Title")
                .description("Event Description")
                .owner(user)
                .creationDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .tags(new HashSet<>(Set.of(tag)))
                .isActive(true)
                .build();

        nonActiveEvent = Event.builder()
                .title("Event Title")
                .description("Event Description")
                .owner(user)
                .creationDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .tags(new HashSet<>(Set.of(tag)))
                .isActive(false)
                .build();

        newEvent = Event.builder()
                .title("New Event")
                .description("Description").owner(user)
                .creationDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(1))
                .isActive(true)
                .build();

        entityManager.flush();
        entityManager.persist(tag);
        entityManager.persist(user);
        entityManager.persist(anotherUser);
        entityManager.persist(event);
        entityManager.persist(nonActiveEvent);
        entityManager.persist(newEvent);
    }

    @Test
    void testGetById() {
        EventData eventData = eventService.getById(event.getId());
        assertThat(eventData).isNotNull();
        assertThat(eventData.title()).isEqualTo(event.getTitle());
    }

    @Test
    void testUpdateEvent() {
        EventData updatedEventData = new EventData(event.getId(), 2L, "Updated Title", event.getDescription(), event.getCreationDate(), event.getEndDate(), event.getIsActive(), List.of(), List.of());
        EventData result = eventService.updateEvent(event.getId(), updatedEventData);
        assertThat(result.title()).isEqualTo("Updated Title");
    }

    @Test
    void testJoinEvent() {
        eventService.joinEvent(anotherUser.getId(), event.getId());
        Event foundEvent = entityManager.find(Event.class, event.getId());
        assertThat(foundEvent.getMembers()).contains(anotherUser);
    }

    @Test
    void testLeaveEvent() {
        eventService.leaveEvent(user.getId(), event.getId());
        Event foundEvent = entityManager.find(Event.class, event.getId());
        assertThat(foundEvent.getMembers()).doesNotContain(user);
    }

    @Test
    void testUpdateEventsActivity() {
        LocalDate expirationDate = LocalDate.now().minusDays(1);
        eventService.updateEventsActivity(expirationDate);
        Event foundEvent = entityManager.find(Event.class, event.getId());
        assertThat(foundEvent.getIsActive()).isFalse();
    }

    @Test
    void testFindAllActive() {
        List<EventData> activeEvents = eventService.findAllActive();
        assertThat(activeEvents).isNotEmpty();
        assertThat(activeEvents).allMatch(EventData::isActive);
    }

    @Test
    void testFindAllNonActive() {
        List<EventData> nonActiveEvents = eventService.findAllNonActive();
        assertThat(nonActiveEvents).isNotEmpty();
        assertThat(nonActiveEvents).noneMatch(EventData::isActive);
    }

    @Test
    void testFindAllByOwnerId() {
        List<EventData> eventsByOwner = eventService.findAllByOwnerId(user.getId());
        assertThat(eventsByOwner).isNotEmpty();
        assertThat(eventsByOwner).allMatch(event -> event.ownerId().equals(user.getId()));
    }

    @Test
    void testAddEventToUser() {
        eventService.addEventToUser(eventMapper.map(newEvent), anotherUser.getId());
        Event foundEvent = entityManager.find(Event.class, newEvent.getId());
        assertThat(foundEvent).isNotNull();
        assertThat(foundEvent.getOwner()).isEqualTo(anotherUser);
    }
}