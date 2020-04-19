<template>
  <v-dialog
    :value="dialog"
    @input="$emit('dialog', false)"
    @keydown.esc="$emit('dialog', false)"
    max-width="75%"
    id="dialog"
  >
    <v-card>
      <v-card-title>
        <span class="headline" data-cy="studentQuestionDetailsTitle">
          {{ studentQuestion.title }}
        </span>
        <v-chip
          class="status"
          :color="getStatusColor(studentQuestion.status)"
          small
        >
          <span data-cy="studentQuestionDetailsStatus">
            {{ studentQuestion.status }}
          </span>
        </v-chip>
      </v-card-title>

      <v-card-text class="text-left">
        <show-student-question :studentQuestion="studentQuestion" />
      </v-card-text>

      <v-card-actions>
        <v-spacer />
        <v-btn dark color="blue darken-1" @click="$emit('dialog')">close</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Vue, Prop, Model } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';
import ShowStudentQuestion from '@/views/student/questions/ShowStudentQuestion.vue';

@Component({
  components: {
    'show-student-question': ShowStudentQuestion
  }
})
export default class ShowStudentQuestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;

  getStatusColor(status: string) {
    if (status === 'REJECTED') return 'red';
    else if (status === 'AWAITING_APPROVAL') return 'orange';
    else return 'green';
  }
}
</script>

<style lang="sass" scoped>
.status
  transform: scale(.8)
.v-dialog__content--active
  z-index: 9999 !important
</style>
