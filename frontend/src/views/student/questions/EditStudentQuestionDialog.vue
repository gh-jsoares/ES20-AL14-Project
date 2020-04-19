<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    max-height="80%"
  >
    <v-card>
      <v-card-title>
        <span class="headline">
          {{
            editStudentQuestion && editStudentQuestion.id === null
              ? 'New StudentQuestion'
              : 'Edit StudentQuestion'
          }}
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="editStudentQuestion">
        <v-container grid-list-md fluid>
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
                rows="10"
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
                rows="10"
                v-model="editStudentQuestion.options[index - 1].content"
                :label="`Option ${index}`"
                :data-cy="`studentQuestionNewOption-${index}-content`"
              ></v-textarea>
            </v-flex>
          </v-layout>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn
          data-cy="cancelButton"
          color="blue darken-1 white--text"
          @click="$emit('dialog', false)"
        >
          Cancel
        </v-btn>
        <v-btn
          color="green darken-1 white--text"
          @click="saveStudentQuestion"
          data-cy="studentQuestionNewSave"
        >
          Save
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Vue } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';
import RemoteServices from '@/services/RemoteServices';

@Component
export default class EditStudentQuestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;

  editStudentQuestion!: StudentQuestion;

  created() {
    this.editStudentQuestion = new StudentQuestion(this.studentQuestion);
  }

  // TODO use EasyMDE with these configs
  // markdownConfigs: object = {
  //   status: false,
  //   spellChecker: false,
  //   insertTexts: {
  //     image: ['![image][image]', '']
  //   }
  // };

  async saveStudentQuestion() {
    if (
      this.editStudentQuestion &&
      (!this.editStudentQuestion.title || !this.editStudentQuestion.content)
    ) {
      await this.$store.dispatch(
        'error',
        'StudentQuestion must have title and content'
      );
      return;
    }

    /* if (this.editStudentQuestion && this.editStudentQuestion.id != null) {
      try {
        const result = await RemoteServices.updateStudentQuestion(this.editStudentQuestion);
        this.$emit('save-student-question', result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    } else  */
    if (this.editStudentQuestion) {
      try {
        const result = await RemoteServices.createStudentQuestion(
          this.editStudentQuestion
        );
        this.$emit('save-student-question', result);
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }
}
</script>

<style lang="sass" scoped>
.status
  transform: scale(.8)
.v-dialog__content--active
  z-index: 9999 !important
</style>
