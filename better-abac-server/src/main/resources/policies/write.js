function personalPhysician(ctx) {
    return policyHelper.hasRelation(ctx.user, 'PERSONAL_PHYSICIAN', ctx.patient);
}

function wardPhysician(ctx) {
    var relations = policyHelper.getRelation(ctx.patient, 'ON_WARD');
    return policyHelper.hasRelation(ctx.user, 'MEMBER_OF', relations);
}

function policy(ctx) {
    return personalPhysician(ctx) || wardPhysician(ctx);
}