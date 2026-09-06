package az.tribe.lifeplanner.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.ArrowLeft
import com.adamglin.phosphoricons.regular.ArrowRight
import com.adamglin.phosphoricons.regular.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import leanlifeplanner.app.shared.generated.resources.Res
import org.jetbrains.compose.resources.stringResource
import leanlifeplanner.app.shared.generated.resources.cd_complete
import leanlifeplanner.app.shared.generated.resources.cd_next
import leanlifeplanner.app.shared.generated.resources.cd_previous

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuestionNavigationBar(
    pagerState: PagerState,
    allQuestions: List<QuestionWithType>,
    answers: Map<String, String>,
    coroutineScope: CoroutineScope,
    onComplete: () -> Unit
) {
    // Current question details
    val currentPage = pagerState.currentPage
    val currentQuestionTitle = allQuestions.getOrNull(currentPage)?.question?.title ?: ""
    val currentQuestionAnswered = answers.containsKey(currentQuestionTitle)
    
    // Navigation state
    val isFirstPage = currentPage == 0
    val isLastPage = currentPage == allQuestions.size - 1
    val allQuestionsAnswered = answers.size == allQuestions.size
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous button
        OutlinedButton(
            onClick = {
                if (!isFirstPage) {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(currentPage - 1)
                    }
                }
            },
            enabled = !isFirstPage,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = PhosphorIcons.Regular.ArrowLeft,
                contentDescription = stringResource(Res.string.cd_previous)
            )
            Text(
                text = "Previous",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
        
        // Next/Complete button
        Button(
            onClick = {
                if (isLastPage) {
                    onComplete()
                } else {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(currentPage + 1)
                    }
                }
            },
            enabled = currentQuestionAnswered || (isLastPage && allQuestionsAnswered),
            modifier = Modifier.weight(1f).padding(start = 8.dp)
        ) {
            Text(
                text = if (isLastPage) "Generate Goals" else "Next",
                modifier = Modifier.padding(end = if (isLastPage) 0.dp else 8.dp)
            )
            if (!isLastPage) {
                Icon(
                    imageVector = PhosphorIcons.Regular.ArrowRight,
                    contentDescription = stringResource(Res.string.cd_next)
                )
            } else {
                Icon(
                    imageVector = PhosphorIcons.Regular.Check,
                    contentDescription = stringResource(Res.string.cd_complete)
                )
            }
        }
    }
}