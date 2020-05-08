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
          Approve "{{ editStudentQuestion.title }}"
        </span>
      </v-card-title>

      <v-card-text class="text-left" v-if="editStudentQuestion">
        <v-container grid-list-md fluid>
          <v-stepper v-model="currentStep" :alt-labels="true">
            <v-stepper-header>
              <v-stepper-step
                :complete="currentStep > 1"
                step="1"
                :editable="true"
              >
                Topics and Image
              </v-stepper-step>
              <v-divider></v-divider>

              <v-stepper-step
                :complete="currentStep > 2"
                step="2"
                :editable="true"
              >
                Content
              </v-stepper-step>
              <v-divider></v-divider>

              <v-stepper-step
                :complete="currentStep > 3"
                step="3"
                :editable="true"
              >
                Review
              </v-stepper-step>
            </v-stepper-header>

            <v-stepper-items>
              <v-stepper-content step="1">
                <v-card
                  class="mb-5"
                  color="lighten-1"
                  height="450px"
                  style="overflow: hidden scroll;padding:10px"
                >
                  <h2>Topics:</h2>
                  <edit-student-question-topics
                    :studentQuestion="editStudentQuestion"
                    :topics="topics"
                    v-on:student-question-changed-topics="
                      onStudentQuestionChangedTopics
                    "
                  />
                  <h2>Image:</h2>
                  <v-file-input
                    show-size
                    dense
                    small-chips
                    @change="handleFileUpload($event, editStudentQuestion)"
                    accept="image/*"
                  />

                  <v-img :src="getImage(editStudentQuestion)" />
                </v-card>
                <v-btn
                  data-cy="stepperContinueContent"
                  color="primary"
                  @click="currentStep = 2"
                >
                  Continue
                </v-btn>
              </v-stepper-content>
              <v-stepper-content step="2">
                <v-card
                  class="mb-5"
                  color="lighten-1"
                  style="overflow: hidden scroll;padding:10px"
                  height="450px"
                >
                  <approve-student-question-content
                    v-model="editStudentQuestion"
                  ></approve-student-question-content>
                </v-card>
                <v-btn text @click="currentStep = 1">Back</v-btn>
                <v-btn
                  data-cy="stepperContinueReview"
                  color="primary"
                  @click="currentStep = 3"
                >
                  Continue
                </v-btn>
              </v-stepper-content>
              <v-stepper-content step="3">
                <v-card
                  class="mb-5"
                  color="lighten-1"
                  style="overflow: hidden scroll;padding:10px"
                  height="450px"
                >
                  <show-edit-student-question
                    :studentQuestion="editStudentQuestion"
                  />
                </v-card>
                <v-btn text @click="currentStep = 2">Back</v-btn>
                <v-btn
                  color="green darken-1 white--text"
                  @click="saveStudentQuestion"
                  data-cy="stepperApprove"
                >
                  Approve
                </v-btn>
              </v-stepper-content>
            </v-stepper-items>
          </v-stepper>
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
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import { Component, Model, Prop, Watch, Vue } from 'vue-property-decorator';
import StudentQuestion from '@/models/management/StudentQuestion';
import RemoteServices from '@/services/RemoteServices';
import Topic from '@/models/management/Topic';
import Image from '@/models/management/Image';
import ApproveStudentQuestionContent from '@/views/teacher/questions/student/ApproveStudentQuestionContent.vue';
import ShowEditStudentQuestion from '@/views/teacher/questions/student/ShowEditStudentQuestion.vue';
import EditStudentQuestionTopics from '@/views/student/questions/EditStudentQuestionTopics.vue';

@Component({
  components: {
    'approve-student-question-content': ApproveStudentQuestionContent,
    'show-edit-student-question': ShowEditStudentQuestion,
    'edit-student-question-topics': EditStudentQuestionTopics
  }
})
export default class ApproveStudentQuestionDialog extends Vue {
  @Model('dialog', Boolean) dialog!: boolean;
  @Prop({ type: StudentQuestion, required: true })
  readonly studentQuestion!: StudentQuestion;

  @Prop({ type: Array, required: true })
  readonly topics!: Topic[];

  editStudentQuestion!: StudentQuestion;

  currentStep: number = 1;

  created() {
    this.updateStudentQuestion();
  }

  @Watch('studentQuestion', { immediate: true, deep: true })
  updateStudentQuestion() {
    this.editStudentQuestion = new StudentQuestion(this.studentQuestion);
  }

  getImage(studentQuestion: StudentQuestion): string {
    if (studentQuestion.image)
      return `${process.env.VUE_APP_ROOT_API}/images/questions/${studentQuestion.image.url}`;
    return '';
  }

  onStudentQuestionChangedTopics(
    studentQuestionId: Number,
    changedTopics: Topic[]
  ) {
    this.$emit(
      'student-question-changed-topics',
      studentQuestionId,
      changedTopics
    );
  }

  async handleFileUpload(event: File, studentQuestion: StudentQuestion) {
    if (studentQuestion.id) {
      try {
        const imageURL = await RemoteServices.uploadImageToStudentQuestion(
          event,
          studentQuestion.id
        );
        const image = new Image(null, imageURL);
        studentQuestion.image = image;
        confirm('Image ' + imageURL + ' was uploaded!');
        this.$emit('student-question-changed-image', studentQuestion.id, image);
        this.updateStudentQuestion();
      } catch (error) {
        await this.$store.dispatch('error', error);
      }
    }
  }

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

    if (this.editStudentQuestion) {
      if (confirm('Are you sure you want to approve this student question?')) {
        try {
          const result = await RemoteServices.approveStudentQuestion(
            this.editStudentQuestion
          );
          this.$emit('approve-student-question', result);
        } catch (error) {
          await this.$store.dispatch('error', error);
        }
      }
    }
  }
}
</script>

<style lang="sass">
span.v-stepper__step__step
  height: 35px !important
  width: 35px !important
  i
    padding: 0 !important
div.v-stepper__label
  display: block !important
</style>

<style lang="sass" scoped>
.v-dialog__content--active
  z-index: 8000 !important
</style>
