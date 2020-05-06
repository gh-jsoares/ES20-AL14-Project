<template>
  <div
    v-if="question"
    v-bind:class="[
      'question-container',
      answer.optionId === null ? 'unanswered' : '',
      answer.optionId !== null &&
      correctAnswer.correctOptionId === answer.optionId
        ? 'correct-question'
        : 'incorrect-question'
    ]"
  >
    <div class="question">
      <span
        @click="decreaseOrder"
        @mouseover="hover = true"
        @mouseleave="hover = false"
        class="square"
      >
        <i v-if="hover && questionOrder !== 0" class="fas fa-chevron-left" />
        <span v-else>{{ questionOrder + 1 }}</span>
      </span>
      <div
        class="question-content"
        v-html="convertMarkDown(question.content, question.image)"
      ></div>
      <div @click="getQuestionDiscussions()" v-if="!discussionsDiv" class="square" data-cy="GetDiscussions">
        <i class="fas fa-comment-alt mt-3"></i>
      </div>
      <div @click="createDiscussion" class="square" data-cy="Open Discussion">
        <i class="fas fa-question" />
      </div>
      <div @click="increaseOrder" class="square">
        <i
          v-if="questionOrder !== questionNumber - 1"
          class="fas fa-chevron-right"
        />
      </div>
    </div>
    <ul class="option-list">
      <li
        v-for="(n, index) in question.options.length"
        :key="index"
        v-bind:class="[
          answer.optionId === question.options[index].optionId ? 'wrong' : '',
          correctAnswer.correctOptionId === question.options[index].optionId
            ? 'correct'
            : '',
          'option'
        ]"
      >
        <i
          v-if="
            correctAnswer.correctOptionId === question.options[index].optionId
          "
          class="fas fa-check option-letter"
        />
        <i
          v-else-if="answer.optionId === question.options[index].optionId"
          class="fas fa-times option-letter"
        />
        <span v-else class="option-letter">{{ optionLetters[index] }}</span>
        <span
          class="option-content"
          v-html="convertMarkDown(question.options[index].content)"
        />
      </li>
    </ul>
    <v-toolbar class="mt-12" v-if="discussionsDiv && discussions[0]"
            color="teal"
            dark
    >
      <v-toolbar-title>Discussions about this question</v-toolbar-title>
    </v-toolbar>
    <v-toolbar class="mt-12" v-else-if="discussionsDiv"
            color="teal"
            dark
    >
      <v-toolbar-title>There are no discussions for this question.</v-toolbar-title>
    </v-toolbar>
    <v-list v-if="discussionsDiv">
      <v-list-group
              v-for="discussion in discussions"
              :key="discussion.messages[0].userName"
              v-model="discussion.active"
      >
        <template v-slot:activator>
            <v-list-item-title v-text="discussion.messages[0].userName + ' asked ' + discussion.messages[0].message"></v-list-item-title>
        </template>

        <v-list-item disabled
                v-for="message in discussion.messages.slice(1,discussion.messages.length)"
                :key="message.message"
                @click=""
        >
          <v-list-item-content>
            <v-list-item-title v-text="message.userName + ' answered ' + message.message"></v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list-group>
    </v-list>
    <create-discussion-dialog
      v-if="createDiscussionDialog"
      v-model="createDiscussionDialog"
      :questionId="question.questionId"
      :questionAnswerId="answer.questionAnswerId"
      :options="question.options"
      :content="question.content"
      :correct="correctAnswer.correctOptionId"
      v-on:create-discussion="discussionCreated"
      v-on:close-dialog="onCloseDialog"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model, Emit } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import StatementQuestion from '@/models/statement/StatementQuestion';
import StatementAnswer from '@/models/statement/StatementAnswer';
import StatementCorrectAnswer from '@/models/statement/StatementCorrectAnswer';
import Image from '@/models/management/Image';
import CreateDiscussionDialog from '@/views/student/discussion/CreateDiscussionDialog.vue';
import RemoteServices from '@/services/RemoteServices';
import { Discussion } from '@/models/management/Discussion';

@Component({
  components: {
    'create-discussion-dialog': CreateDiscussionDialog
  }
})
export default class ResultComponent extends Vue {
  @Model('questionOrder', Number) questionOrder: number | undefined;
  @Prop(StatementQuestion) readonly question!: StatementQuestion;
  @Prop(StatementCorrectAnswer) readonly correctAnswer!: StatementCorrectAnswer;
  @Prop(StatementAnswer) readonly answer!: StatementAnswer;
  @Prop() readonly questionNumber!: number;
  @Prop() discussionsDiv!:boolean;
  hover: boolean = false;
  optionLetters: string[] = ['A', 'B', 'C', 'D'];
  createDiscussionDialog: boolean = false
  discussions: Discussion[] = [];

  @Emit()
  increaseOrder() {
    return 1;
  }

  @Emit()
  decreaseOrder() {
    return 1;
  }

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  createDiscussion() {
    this.createDiscussionDialog = true;
  }

  onCloseDialog() {
    this.createDiscussionDialog = false;
  }

  discussionCreated() {
    this.createDiscussionDialog = false;
    this.createDiscussionDialog = false;
    this.createDiscussionDialog = false;
  }

  async getQuestionDiscussions() {
    try {
      this.discussions = await RemoteServices.getQuestionDiscussions(this.question.questionId, this.answer.questionAnswerId);
      this.discussionsDiv = true;
    } catch (error) {
      await this.$store.dispatch('error', error);
    }
  }
}
</script>

<style lang="scss" scoped>

.unanswered {
  .question {
    background-color: #761515 !important;
    color: #fff !important;
  }
  .correct {
    .option-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #333333 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}

.correct-question {
  .question .question-content {
    background-color: #285f23 !important;
    color: white !important;
  }
  .question .square {
    background-color: #285f23 !important;
    color: white !important;
  }
  .correct {
    .option-content {
      background-color: #299455;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #299455 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}

.incorrect-question {
  .question .question-content {
    background-color: #761515 !important;
    color: white !important;
  }
  .question .square {
    background-color: #761515 !important;
    color: white !important;
  }
  .wrong {
    .option-content {
      background-color: #cf2323;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #cf2323 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
  .correct {
    .option-content {
      background-color: #333333;
      color: rgb(255, 255, 255) !important;
    }

    .option-letter {
      background-color: #333333 !important;
      color: rgb(255, 255, 255) !important;
    }
  }
}
</style>
