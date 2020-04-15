<template>
  <div class="container">
    <h2>Discussions To Answer</h2>
    <ul>
      <li class="list-header">
        <div class="col">Discussion Question Title</div>
        <div class="col last-col"></div>
      </li>
      <li
        class="list-row"
        v-for="discussion in discussions"
        :key="discussion.question.title"
        @click="answerDiscussion(discussion)"
      >
        <div class="col">
          {{ discussion.question.title }}
        </div>
        <div class="col last-col">
          <i class="fas fa-chevron-circle-right"></i>
        </div>
      </li>
    </ul>
    <answer-discussion-dialog
      v-if="currentDiscussion"
      v-model="editDiscussionDialog"
      :discussion="currentDiscussion"
      v-on:answer-discussion="onAnsweringDiscussion"
      v-on:close-dialog="onCloseDialog"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Discussion } from '@/models/management/Discussion';
import AnswerDiscussionDialog from '@/views/teacher/discussions/AnswerDiscussionDialog.vue';

@Component({
  components: {
    'answer-discussion-dialog': AnswerDiscussionDialog
  }
})
export default class DiscussionsView extends Vue {
  discussions: Discussion[] = [];
  currentDiscussion: Discussion | null = null;
  editDiscussionDialog: boolean = false;
  search: string = '';
  headers: object = [
    {
      text: 'Question',
      value: 'question.content',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Student username',
      value: 'studentName',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Question by student',
      value: 'messageFromStudent',
      align: 'center',
      width: '10%'
    },
    {
      text: 'Actions',
      value: 'action',
      align: 'center',
      sortable: false,
      width: '20%'
    }
  ];

  async created() {
    await this.$store.dispatch('loading');
    try {
      this.discussions = await RemoteServices.getDiscussions();
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
    await this.$store.dispatch('clearLoading');
  }

  async onAnsweringDiscussion(discussion: Discussion) {
    this.discussions = this.discussions.filter(
      disc => disc.id !== discussion.id
    );
    this.editDiscussionDialog = false;
    this.currentDiscussion = null;
  }

  onCloseDialog() {
    this.editDiscussionDialog = false;
    this.currentDiscussion = null;
  }

  async answerDiscussion(discussion: Discussion) {
    this.currentDiscussion = new Discussion(discussion);
    this.currentDiscussion.teacherAnswer = undefined;
    this.editDiscussionDialog = true;
  }
}
</script>

<style lang="scss" scoped>
.container {
  max-width: 1000px;
  margin-left: auto;
  margin-right: auto;
  padding-left: 10px;
  padding-right: 10px;
}

ul {
  overflow: hidden;
  padding: 0 5px;

  li {
    border-radius: 3px;
    padding: 15px 10px;
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
  }

  .list-header {
    background-color: #1976d2;
    color: white;
    font-size: 14px;
    text-transform: uppercase;
    letter-spacing: 0.03em;
    text-align: center;
  }

  .col {
    flex-basis: 25% !important;
    margin: auto; /* Important */
    text-align: center;
  }

  .list-row {
    background-color: #ffffff;
    box-shadow: 0 0 9px 0 rgba(0, 0, 0, 0.1);
    display: flex;
  }
}
</style>
