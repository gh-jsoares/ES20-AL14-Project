<template>
  <v-card class="table">
    <v-data-table
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
          <!-- <v-btn color="primary" dark @click="newStudentQuestion">New StudentQuestion</v-btn> -->
        </v-card-title>
      </template>

      <template v-slot:item.content="{ item }">
        <p v-html="convertMarkDownNoFigure(item.content, null)" />
      </template>

      <template v-slot:item.topics="{ item }">
        <!-- <edit-studentQuestion-topics
          :studentQuestion="item"
          :topics="topics"
        /> -->
        <span>{{ item.topics }} | TODO</span>
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
        {{ item.id }}TODO
        <!-- <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon
              small
              class="mr-2"
              v-on="on"
              @click="showStudentQuestionDialog(item)"
              >visibility</v-icon
            >
          </template>
          <span>Show StudentQuestion</span>
        </v-tooltip>
        <v-tooltip bottom v-if="item.numberOfAnswers === 0">
          <template v-slot:activator="{ on }">
            <v-icon small class="mr-2" v-on="on" @click="editStudentQuestion(item)"
              >edit</v-icon
            >
          </template>
          <span>Edit StudentQuestion</span>
        </v-tooltip> -->
      </template>
    </v-data-table>
  </v-card>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { convertMarkDownNoFigure } from '@/services/ConvertMarkdownService';
import Topic from '@/models/management/Topic';
import Image from '@/models/management/Image';
import StudentQuestion from '@/models/management/StudentQuestion';

@Component
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
