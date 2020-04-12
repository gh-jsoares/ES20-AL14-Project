<template>
    <v-card class="table">
        <v-data-table
                :headers="headers"
                :items="discussions"
                :search="search"
                disable-pagination
                :hide-default-footer="true"
                :mobile-breakpoint="0"
                multi-sort
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

            <template v-slot:item.action="{ item }">
                <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                        <v-icon
                                small
                                class="mr-2"
                                v-on="on"
                                @click="answerDiscussion(item)"
                                data-cy="answerDiscussion"
                        >cached</v-icon
                        >
                    </template>
                    <span>Answer discussion</span>
                </v-tooltip>
            </template>
        </v-data-table>

        <answer-discussion-dialog
                v-if="currentDiscussion"
                v-model="editDiscussionDialog"
                :discussion="currentDiscussion"
                v-on:answer-discussion="onAnsweringDiscussion"
                v-on:close-dialog="onCloseDialog"
        />
    </v-card>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator';
import RemoteServices from '@/services/RemoteServices';
import { Discussion } from '@/models/management/Discussion';
import AnswerDiscussionDialog from '@/views/teacher/discussions/AnswerDiscussionDialog.vue';

@Component ( {
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
        this.discussions.splice(this.discussions.indexOf(discussion), 1);
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

<style lang="scss" scoped></style>