<template>
  <v-card class="table">
    <v-data-table
      data-cy="student-questions-table"
      :headers="headers"
      :custom-filter="customFilter"
      :items="studentQuestions"
      :search="search"
      multi-sort
      :mobile-breakpoint="0"
      :items-per-page="15"
      :footer-props="{ itemsPerPageOptions: [15, 30, 50, 100] }"
    >
      <template v-slot:top>
        <v-card-title>
          <v-text-field
            v-model="search"
            append-icon="search"
            label="Search"
            class="mx-2"
          />

          <v-spacer />
          <v-btn
            color="primary"
            @click="newStudentQuestion"
            dark
            data-cy="studentQuestionNew"
          >
            New Student Question
          </v-btn>
        </v-card-title>
      </template>

      <template v-slot:item.title="{ item }">
        <span data-cy="studentQuestionViewTitle">{{ item.title }}</span>
      </template>

      <template v-slot:item.content="{ item }">
        <p v-html="convertMarkDownNoFigure(item.content, null)" />
      </template>

      <template v-slot:item.topics="{ item }">
        <edit-student-question-topics
          :studentQuestion="item"
          :topics="topics"
          v-on:student-question-changed-topics="onStudentQuestionChangedTopics"
        />
      </template>

      <template v-slot:item.status="{ item }">
        <v-chip :color="getStatusColor(item.status)" small>
          <span>{{ item.status }}</span>
        </v-chip>
      </template>

      <template v-slot:item.image="{ item }">
        <v-file-input
          show-size
          dense
          small-chips
          @change="handleFileUpload($event, item)"
          accept="image/*"
        />
      </template>

      <template v-slot:item.action="{ item }">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              data-cy="viewStudentQuestionDetails"
              small
              class="mr-2"
              v-on="on"
              @click="showStudentQuestionDialog(item)"
              >visibility</v-icon
            >
          </template>
          <span>View Details</span>
        </v-tooltip>
      </template>
    </v-data-table>
    <edit-student-question-dialog
      v-if="currentStudentQuestion"
      v-model="editStudentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      v-on:save-student-question="onSaveStudentQuestion"
    />
    <show-student-question-dialog
      v-if="currentStudentQuestion"
      v-model="studentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      v-on:close-show-student-question-dialog="onCloseShowStudentQuestionDialog"
    />
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDownNoFigure } from '@/services/ConvertMarkdownService';
import Topic from '@/models/management/Topic';
import Image from '@/models/management/Image';
import StudentQuestion from '@/models/management/StudentQuestion';

import ShowStudentQuestionDialog from '@/views/student/questions/ShowStudentQuestionDialog.vue';
import EditStudentQuestionDialog from '@/views/student/questions/EditStudentQuestionDialog.vue';
import EditStudentQuestionTopics from '@/views/student/questions/EditStudentQuestionTopics.vue';

@Component({
  components: {
    'show-student-question-dialog': ShowStudentQuestionDialog,
    'edit-student-question-dialog': EditStudentQuestionDialog,
    'edit-student-question-topics': EditStudentQuestionTopics
  }
})
export default class StudentQuestionsView extends Vue {
  studentQuestions: StudentQuestion[] = [];
  topics: Topic[] = [];
  currentStudentQuestion: StudentQuestion | null = null;
  editStudentQuestionDialog: boolean = false;
  studentQuestionDialog: boolean = false;
  search: string = '';
  statusList = ['AWAITING_APPROVAL', 'ACCEPTED', 'REJECTED'];

  headers: object = [
    { text: 'Title', value: 'title', align: 'center' },
    { text: 'Content', value: 'content', align: 'left' },
    {
      text: 'Topics',
      value: 'topics',
      align: 'center',
      sortable: false
    },
    { text: 'Status', value: 'status', align: 'center' },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center'
    },
    {
      text: 'Image',
      value: 'image',
      align: 'center',
      sortable: false
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      [this.topics, this.studentQuestions] = await Promise.all([
        RemoteServices.getTopics(),
        RemoteServices.getStudentQuestionsAsStudent()
      ]);
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  getStatusColor(status: string) {
    if (status === 'REJECTED') return 'red';
    else if (status === 'AWAITING_APPROVAL') return 'orange';
    else return 'green';
  }

  customFilter(
    value: string,
    search: string,
    studentQuestion: StudentQuestion
  ) {
    // noinspection SuspiciousTypeOfGuard,SuspiciousTypeOfGuard
    return (
      search != null &&
      JSON.stringify(studentQuestion)
        .toLowerCase()
        .indexOf(search.toLowerCase()) !== -1
    );
  }

  convertMarkDownNoFigure(text: string, image: Image | null = null): string {
    return convertMarkDownNoFigure(text, image);
  }

  showStudentQuestionDialog(studentQuestion: StudentQuestion) {
    this.currentStudentQuestion = studentQuestion;
    this.studentQuestionDialog = true;
  }

  onCloseShowStudentQuestionDialog() {
    this.studentQuestionDialog = false;
  }

  async handleFileUpload(event: File, studentQuestion: StudentQuestion) {
    if (studentQuestion.id) {
      try {
        const imageURL = await RemoteServices.uploadImageToStudentQuestion(
          event,
          studentQuestion.id
        );
        studentQuestion.image = new Image();
        studentQuestion.image.url = imageURL;
        confirm('Image ' + imageURL + ' was uploaded!');
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  @Watch('editStudentQuestionDialog')
  closeError() {
    if (!this.editStudentQuestionDialog) {
      this.currentStudentQuestion = null;
    }
  }

  newStudentQuestion() {
    this.currentStudentQuestion = new StudentQuestion();
    this.editStudentQuestionDialog = true;
  }

  async onSaveStudentQuestion(studentQuestion: StudentQuestion) {
    this.studentQuestions = this.studentQuestions.filter(
      q => q.id !== studentQuestion.id
    );
    this.studentQuestions.unshift(studentQuestion);
    this.editStudentQuestionDialog = false;
    this.currentStudentQuestion = null;
  }

  onStudentQuestionChangedTopics(
    studentQuestionId: Number,
    changedTopics: Topic[]
  ) {
    let studentQuestion = this.studentQuestions.find(
      (studentQuestion: StudentQuestion) =>
        studentQuestion.id == studentQuestionId
    );
    if (studentQuestion) {
      studentQuestion.topics = changedTopics;
    }
  }
}
</script>

<style lang="scss" scoped>
.studentQuestion-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 200px !important;
  }
}
.option-textarea {
  text-align: left;

  .CodeMirror,
  .CodeMirror-scroll {
    min-height: 100px !important;
  }
}
</style>
