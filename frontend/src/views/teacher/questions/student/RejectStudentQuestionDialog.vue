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
        <span class="headline">{{ studentQuestion.title }}</span>
      </v-card-title>

      <v-card-text class="text-left" v-if="studentQuestion">
        <v-container grid-list-md fluid>
          <v-layout column wrap>
            <v-flex xs24 sm12 md12>
              <v-textarea
                outline
                rows="10"
                color="red"
                v-model="explanation"
                label="Explanation"
                data-cy="studentQuestionRejectExplanation"
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
          color="red darken-1 white--text"
          @click="rejectStudentQuestion"
          data-cy="rejectStudentQuestion"
        >
          Reject
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

  explanation!: string | null;

  created() {
    this.explanation = null;
  }

  async rejectStudentQuestion() {
    if (!this.explanation) {
      await this.$store.dispatch('error', 'Explanation cannot be empty');
      return;
    }

    if (this.studentQuestion) {
      try {
        const result = await RemoteServices.rejectStudentQuestion(
          this.studentQuestion,
          this.explanation
        );
        this.$emit('reject-student-question', result);
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
