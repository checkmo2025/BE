package checkmo.clubManagement.internal.repository;

import checkmo.clubManagement.internal.entity.Club;
import checkmo.clubManagement.web.dto.ClubRequestDTO;
import java.util.List;

public interface ClubRepositoryCustom {
    List<Club> searchClubs(ClubRequestDTO.ClubSearchFilter filter, Long cursorId, Integer size);
}
