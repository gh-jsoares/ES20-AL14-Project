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
        </v-card-title>
      </template>

      <template v-slot:item.title="{ item }">
        <span data-cy="studentQuestionViewTitle">{{ item.title }}</span>
      </template>

      <template v-slot:item.content="{ item }">
        <p v-html="convertMarkDownNoFigure(item.content, null)" />
      </template>

      <template v-slot:item.topics="{ item }">
        <span v-if="item.topics.length == 0">No topics</span>
        <v-chip v-for="topic in item.topics" :key="topic.id">
          {{ topic.name }}
        </v-chip>
      </template>

      <template v-slot:item.status="{ item }">
        <v-chip :color="getStatusColor(item.status)" small>
          <span>{{ item.status }}</span>
        </v-chip>
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

        <template v-if="item.status === 'AWAITING_APPROVAL'">
          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                data-cy="approveStudentQuestion"
                small
                class="mr-2 green--text"
                v-on="on"
                @click="approveStudentQuestion(item)"
                >check</v-icon
              >
            </template>
            <span>Approve</span>
          </v-tooltip>

          <v-tooltip bottom>
            <template v-slot:activator="{ on }">
              <v-icon
                data-cy="rejectStudentQuestion"
                small
                class="mr-2 red--text"
                v-on="on"
                @click="openRejectStudentQuestionDialog(item)"
                >close</v-icon
              >
            </template>
            <span>Reject</span>
          </v-tooltip>
        </template>
      </template>
    </v-data-table>
    <reject-student-question-dialog
      v-if="currentStudentQuestion"
      v-model="rejectStudentQuestionDialog"
      :studentQuestion="currentStudentQuestion"
      v-on:reject-student-question="onRejectStudentQuestion"
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
import Image from '@/models/management/Image';
import StudentQuestion from '@/models/management/StudentQuestion';

import ShowStudentQuestionDialog from '@/views/student/questions/ShowStudentQuestionDialog.vue';
import RejectStudentQuestionDialog from '@/views/teacher/questions/student/RejectStudentQuestionDialog.vue';

@Component({
  components: {
    'show-student-question-dialog': ShowStudentQuestionDialog,
    'reject-student-question-dialog': RejectStudentQuestionDialog
  }
})
export default class StudentQuestionsView extends Vue {
  studentQuestions: StudentQuestion[] = [];
  currentStudentQuestion: StudentQuestion | null = null;
  rejectStudentQuestionDialog: boolean = false;
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
    { text: 'Student', value: 'creatorUsername', align: 'center' },
    {
      text: 'Creation Date',
      value: 'creationDate',
      align: 'center'
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
      this.studentQuestions = await RemoteServices.getStudentQuestionsAsTeacher();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  getStatusColor(status: string) {
    if (status === 'REJECTED') return 'red white--text';
    else if (status === 'AWAITING_APPROVAL') return 'orange white--text';
    else return 'green white--text';
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

  async approveStudentQuestion(studentQuestion: StudentQuestion) {
    if (confirm('Are you sure you want to approve this student question?')) {
      try {
        const newStudentQuestion = await RemoteServices.approveStudentQuestion(
          studentQuestion
        );
        this.updateStudentQuestion(newStudentQuestion);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

  updateStudentQuestion(studentQuestion: StudentQuestion) {
    this.studentQuestions = this.studentQuestions.filter(
      q => q.id !== studentQuestion.id
    );
    this.studentQuestions.unshift(studentQuestion);
  }

  @Watch('rejectStudentQuestionDialog')
  closeError() {
    if (!this.rejectStudentQuestionDialog) {
      this.currentStudentQuestion = null;
    }
  }

  openRejectStudentQuestionDialog(studentQuestion: StudentQuestion) {
    this.currentStudentQuestion = studentQuestion;
    this.rejectStudentQuestionDialog = true;
  }

  async onRejectStudentQuestion(studentQuestion: StudentQuestion) {
    this.updateStudentQuestion(studentQuestion);
    this.rejectStudentQuestionDialog = false;
    this.currentStudentQuestion = null;
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
