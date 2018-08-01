package utils;

import com.intellij.lang.Language;
import com.intellij.lang.LanguageParserDefinitions;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.PsiClassImplUtil;
import com.intellij.psi.util.PsiClassUtil;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.psi.*;
import org.jetbrains.kotlin.psi.psiUtil.KtPsiUtilKt;

/**
 * Created by mjj on 2018/7/28
 */
public class JavaFileWriteUtils {

    public JavaFileWriteUtils(Project project, PsiFile psiFile) {
        PsiElementFactory psiElementFactory = JavaPsiFacade.getElementFactory(project);

        KtPsiFactory ktPsiFactory = KtPsiFactoryKt.KtPsiFactory(project);
        // 创建变量
        KtProperty ktProperty = ktPsiFactory.createProperty("private","test","View",false,"lazy {}");

        final PsiClass psiClass = PsiTreeUtil.findChildOfAnyType(psiFile.getOriginalElement(), PsiClass.class);
        psiClass.add(ktProperty);

        WriteCommandAction.runWriteCommandAction(project, new Runnable() {
            @Override
            public void run() {

            }
        });
    }
}
