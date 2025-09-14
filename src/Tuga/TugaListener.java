package Tuga;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link TugaParser}.
 */
public interface TugaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link TugaParser#prog}.
	 * @param ctx the parse tree
	 */
	void enterProg(TugaParser.ProgContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#prog}.
	 * @param ctx the parse tree
	 */
	void exitProg(TugaParser.ProgContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#decl}.
	 * @param ctx the parse tree
	 */
	void enterDecl(TugaParser.DeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#decl}.
	 * @param ctx the parse tree
	 */
	void exitDecl(TugaParser.DeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#argdecl}.
	 * @param ctx the parse tree
	 */
	void enterArgdecl(TugaParser.ArgdeclContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#argdecl}.
	 * @param ctx the parse tree
	 */
	void exitArgdecl(TugaParser.ArgdeclContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#func}.
	 * @param ctx the parse tree
	 */
	void enterFunc(TugaParser.FuncContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#func}.
	 * @param ctx the parse tree
	 */
	void exitFunc(TugaParser.FuncContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Write}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterWrite(TugaParser.WriteContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Write}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitWrite(TugaParser.WriteContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Block}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterBlock(TugaParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Block}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitBlock(TugaParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code While}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterWhile(TugaParser.WhileContext ctx);
	/**
	 * Exit a parse tree produced by the {@code While}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitWhile(TugaParser.WhileContext ctx);
	/**
	 * Enter a parse tree produced by the {@code IfElse}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterIfElse(TugaParser.IfElseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code IfElse}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitIfElse(TugaParser.IfElseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Affect}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterAffect(TugaParser.AffectContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Affect}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitAffect(TugaParser.AffectContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Return}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterReturn(TugaParser.ReturnContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Return}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitReturn(TugaParser.ReturnContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StatFunctionCall}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterStatFunctionCall(TugaParser.StatFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StatFunctionCall}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitStatFunctionCall(TugaParser.StatFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Empty}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void enterEmpty(TugaParser.EmptyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Empty}
	 * labeled alternative in {@link TugaParser#stat}.
	 * @param ctx the parse tree
	 */
	void exitEmpty(TugaParser.EmptyContext ctx);
	/**
	 * Enter a parse tree produced by {@link TugaParser#blck}.
	 * @param ctx the parse tree
	 */
	void enterBlck(TugaParser.BlckContext ctx);
	/**
	 * Exit a parse tree produced by {@link TugaParser#blck}.
	 * @param ctx the parse tree
	 */
	void exitBlck(TugaParser.BlckContext ctx);
	/**
	 * Enter a parse tree produced by the {@code LessGrtr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterLessGrtr(TugaParser.LessGrtrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code LessGrtr}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitLessGrtr(TugaParser.LessGrtrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Or}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterOr(TugaParser.OrContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Or}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitOr(TugaParser.OrContext ctx);
	/**
	 * Enter a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAddSub(TugaParser.AddSubContext ctx);
	/**
	 * Exit a parse tree produced by the {@code AddSub}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAddSub(TugaParser.AddSubContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Parens}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterParens(TugaParser.ParensContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Parens}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitParens(TugaParser.ParensContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Var}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterVar(TugaParser.VarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Var}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitVar(TugaParser.VarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code EqDiff}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterEqDiff(TugaParser.EqDiffContext ctx);
	/**
	 * Exit a parse tree produced by the {@code EqDiff}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitEqDiff(TugaParser.EqDiffContext ctx);
	/**
	 * Enter a parse tree produced by the {@code UminusUnot}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterUminusUnot(TugaParser.UminusUnotContext ctx);
	/**
	 * Exit a parse tree produced by the {@code UminusUnot}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitUminusUnot(TugaParser.UminusUnotContext ctx);
	/**
	 * Enter a parse tree produced by the {@code String}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterString(TugaParser.StringContext ctx);
	/**
	 * Exit a parse tree produced by the {@code String}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitString(TugaParser.StringContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Int}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterInt(TugaParser.IntContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Int}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitInt(TugaParser.IntContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ExprFunctionCall}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterExprFunctionCall(TugaParser.ExprFunctionCallContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ExprFunctionCall}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitExprFunctionCall(TugaParser.ExprFunctionCallContext ctx);
	/**
	 * Enter a parse tree produced by the {@code MultDivMod}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterMultDivMod(TugaParser.MultDivModContext ctx);
	/**
	 * Exit a parse tree produced by the {@code MultDivMod}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitMultDivMod(TugaParser.MultDivModContext ctx);
	/**
	 * Enter a parse tree produced by the {@code And}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterAnd(TugaParser.AndContext ctx);
	/**
	 * Exit a parse tree produced by the {@code And}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitAnd(TugaParser.AndContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Real}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterReal(TugaParser.RealContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Real}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitReal(TugaParser.RealContext ctx);
	/**
	 * Enter a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void enterBoolean(TugaParser.BooleanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code Boolean}
	 * labeled alternative in {@link TugaParser#expr}.
	 * @param ctx the parse tree
	 */
	void exitBoolean(TugaParser.BooleanContext ctx);
}