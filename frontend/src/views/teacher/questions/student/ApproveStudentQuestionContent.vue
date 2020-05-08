<template>
  <v-layout column wrap>
    <v-flex xs24 sm12 md8>
      <v-text-field
        v-model="editStudentQuestion.title"
        label="Title"
        data-cy="studentQuestionNewTitle"
      />
    </v-flex>
    <v-flex xs24 sm12 md12>
      <v-textarea
        outline
        rows="3"
        v-model="editStudentQuestion.content"
        label="StudentQuestion"
        data-cy="studentQuestionNewContent"
      ></v-textarea>
    </v-flex>
    <v-flex
      xs24
      sm12
      md12
      v-for="index in editStudentQuestion.options.length"
      :key="index"
    >
      <v-switch
        v-model="editStudentQuestion.options[index - 1].correct"
        class="ma-4"
        label="Correct"
        :data-cy="`studentQuestionNewOption-${index}-correct`"
      />
      <v-textarea
        outline
        rows="3"
        v-model="editStudentQuestion.options[index - 1].content"
        :label="`Option ${index}`"
        :data-cy="`studentQuestionNewOption-${index}-content`"
      ></v-textarea>
    </v-flex>
  </v-layout>
</template>
<script lang="ts">
import { Component, PropSync, Vue, Model } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';

@Component
export default class ApproveStudentQuestionDialog extends Vue {
  @Model('studentQuestion', StudentQuestion)
  editStudentQuestion!: StudentQuestion;
}
</script>
