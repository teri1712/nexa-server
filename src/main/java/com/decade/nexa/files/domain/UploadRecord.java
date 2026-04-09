package com.decade.nexa.files.domain;

import com.decade.nexa.files.domain.events.UploadCompleted;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.AbstractAggregateRoot;

@Entity
@NoArgsConstructor
@Getter
@Table(name = "upload_record")
public class UploadRecord extends AbstractAggregateRoot<UploadRecord> {
      @Id
      @Column(name = "id")
      private String key;


      @Column(columnDefinition = "upload_progress")
      @JdbcTypeCode(SqlTypes.NAMED_ENUM)
      private UploadProgress progress;

      public UploadRecord(String key) {
            this.key = key;
            this.progress = UploadProgress.UPLOADING;
      }

      @Version
      private Integer version;


      public void complete() {
            this.progress = UploadProgress.COMPLETED;
            registerEvent(new UploadCompleted(key));
      }
}
