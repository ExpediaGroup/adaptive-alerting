package com.expedia.adaptivealerting.modelservice.repo;

import com.expedia.adaptivealerting.modelservice.entity.Metric;
import com.expedia.adaptivealerting.modelservice.entity.Tag;
import com.expedia.adaptivealerting.modelservice.entity.projection.InlineType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

/**
 * Spring Data repository for tags.
 *
 * @author tbahl
 */
@RepositoryRestResource(excerptProjection = InlineType.class)
public interface TagRepository extends PagingAndSortingRepository<Tag, Long> {


    @Query(nativeQuery = true, value = "SELECT m.* FROM metric m INNER JOIN metric_tag_mapper mm on mm.metric_id = m.id  INNER JOIN tag t on t.id = mm.tag_id WHERE t.ukey = :ukey AND t.uvalue = :uvalue")
    List<Metric> findByTagContaining(@Param("ukey") String ukey, @Param("uvalue") String uvalue);


    /**
     * Finds a tag when matched with ukey and uvalue
     */
    List<Tag> findByUkeyContainingAndUvalueContaining(String ukey, String uvalue);

    @Override
    Tag save(Tag tag);

}
