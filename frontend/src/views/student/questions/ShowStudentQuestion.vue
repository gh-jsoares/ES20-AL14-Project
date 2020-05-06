<template>
  <div>
    <div class="review" v-if="editStudentQuestion.status !== 'AWAITING_APPROVAL'">
      <p data-cy="studentQuestionDetailsReview">
        Last Reviewed by
        {{ editStudentQuestion.lastReviewerUsername }}
        on
        {{ editStudentQuestion.reviewedDate }}
      </p>
      <template v-if="editStudentQuestion.status === 'REJECTED'">
        <h4>Reason:</h4>
        <v-alert data-cy="studentQuestionDetailsRejected" type="error">
          {{ editStudentQuestion.rejectedExplanation }}
        </v-alert>
      </template>
    </div>

    <span
      class="student-question-content"
      data-cy="studentQuestionDetailsContent"
      v-html="convertMarkDown(editStudentQuestion.content, editStudentQuestion.image)"
    />
    <ul>
      <li v-for="option in editStudentQuestion.options" :key="option.number">
        <span
          :data-cy="`studentQuestionDetailsOptionCorrect`"
          v-if="option.correct"
          v-html="convertMarkDown('**[★]** ', null)"
        />
        <span
          :data-cy="`studentQuestionDetailsOption`"
          v-html="convertMarkDown(option.content, null)"
          v-bind:class="[option.correct ? 'font-weight-bold' : '']"
        />
      </li>
    </ul>
    <br />
  </div>
</template>

<script lang="ts">
import { Component, Vue, Prop, Watch } from 'vue-property-decorator';
import { convertMarkDown } from '@/services/ConvertMarkdownService';
import StudentQuestion from '@/models/management/StudentQuestion';
import Image from '@/models/management/Image';

@Component
export default class ShowStudentQuestion extends Vue {
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;

  readonly editStudentQuestion: StudentQuestion = this.editStudentQuestionData();

  convertMarkDown(text: string, image: Image | null = null): string {
    return convertMarkDown(text, image);
  }

  @Watch('syncedStudentQuestion')
  editStudentQuestionData() {
    return this.studentQuestion;
  }
}
</script>

<style lang="sass">
.review
  padding-bottom: 5px
  border-bottom: 1px solid #333333
  p
    margin: 0
span.student-question-content
  max-width: 100%
  img
    max-width: 100% !important
</style>
