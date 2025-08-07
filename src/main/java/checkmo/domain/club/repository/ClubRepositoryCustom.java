package checkmo.domain.club.repository;

import checkmo.domain.club.entity.Club;

import java.util.List;

public interface ClubRepositoryCustom {
    List<Club> searchClubs(String keyword, int region, int participants, Long cursorId, Integer PAGE_SIZE);
}
